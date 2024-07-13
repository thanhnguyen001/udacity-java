package com.example.demo;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SareetaApplicationTests {

    private static final String CREATE_USER_API = "/api/user/create";

    private static final String LOGIN_API = "/login";

    private static final String GET_USER_BY_ID_API = "/api/user/id";

    private static final String CREATE_ITEM_API = "/api/item/create";

    private static final String GET_ALL_ITEMS_API = "/api/item";

    private static final String GET_ITEM_API = "/api/item";

    private static final String GET_ITEM_BY_NAME = "/api/item/name";

    private static final String ADD_TO_CART_API = "/api/cart/addToCart";
    private static final String REMOVE_FROM_CART_API = "/api/cart/removeFromCart";

    private static final String ORDER_API = "/api/order/submit";

    private static final String ORDER_HISTORY_API = "/api/order/history";

    private static String TOKEN = "";

    @Autowired
    private MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeAll
    public void createNewUser() throws Exception {
        mockPostRequest(userMock(), CREATE_USER_API);
        MvcResult loginResult = mockPostRequest(userMock(), LOGIN_API);
        TOKEN = (String) loginResult.getResponse().getHeaderValue("Authorization");
    }

    /**
     * mockPostRequest
     *
     * @param postData postData
     * @param url      url
     * @return MvcResult
     * @throws Exception Exception
     */
    private MvcResult mockPostRequest(Object postData, String url) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", TOKEN);
        RequestBuilder request = post(url)
                .headers(headers)
                .accept(MediaType.ALL_VALUE)
                .locale(Locale.US)
                .content(objectMapper.writeValueAsString(postData))
                .contentType(MediaType.APPLICATION_JSON);
        return mvc
                .perform(request)
                .andDo(print())
                .andReturn();
    }

    /**
     * mockGetRequest
     *
     * @param url url
     * @return MvcResult
     * @throws Exception Exception
     */
    private MvcResult mockGetRequest(String url) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", TOKEN);
        RequestBuilder request = get(url)
                .headers(headers)
                .accept(MediaType.ALL_VALUE)
                .locale(Locale.US)
                .contentType(MediaType.APPLICATION_JSON);
        return mvc
                .perform(request)
                .andDo(print())
                .andReturn();
    }

    /**
     * convertResponseContentToJson
     *
     * @param mvcResult mvcResult
     * @return JSONObject
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     * @throws ParseException               ParseException
     */
    private JSONObject convertResponseContentToJson(MvcResult mvcResult) throws UnsupportedEncodingException, ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(mvcResult.getResponse().getContentAsString());
    }

    /**
     * createMockCartRequest
     *
     * @return ModifyCartRequest
     */
    private ModifyCartRequest createMockCartRequest() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(userMock().getUsername());
        request.setItemId(3);
        request.setQuantity(1);
        return request;
    }

    /**
     * createMockCartData
     *
     * @return Cart
     */
    private Cart createCartMock() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotal(BigDecimal.ONE);
        cart.getItems().add(createMockItem());
        return cart;
    }

    /**
     * createMockItemData
     *
     * @return Item
     */
    private Item createMockItem() {
        Item item = new Item();
        item.setDescription("Item1");
        item.setName("Item1 Name");
        item.setPrice(new BigDecimal(1000));
        return item;
    }

    /**
     * createMockOrderData
     *
     * @return UserOrder
     */
    private UserOrder createMockOrderData() {
        UserOrder userOrder = new UserOrder();
        userOrder.setUser(userMock());
        return userOrder;
    }

    /**
     * createMockUserData
     *
     * @return User
     */
    private User userMock() {
        User user = new User();
        user.setPassword("User1");
        user.setUsername("User1 Name");
        user.setCart(createCartMock());
        return user;
    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        MvcResult mvcResult = mockPostRequest(userMock(), CREATE_USER_API);
        MvcResult loginResult = mockPostRequest(userMock(), LOGIN_API);
        TOKEN = (String) loginResult.getResponse().getHeaderValue("Authorization");
        User user = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                User.class
        );
        assertNotNull(user);
        assertEquals(userMock().getUsername(), user.getUsername());
    }

    @Test
    public void testCreateUserFailedByMissPassword() throws Exception {
        User user = userMock();
        user.setPassword("");
        MvcResult mvcResult = mockPostRequest(user, CREATE_USER_API);
        assertEquals(400, mvcResult.getResponse().getStatus());
    }

    @Test
    public void testCreateUserFailedByPasswordNotMinLength() throws Exception {
        User user = userMock();
        user.setPassword("111");
        MvcResult mvcResult = mockPostRequest(user, CREATE_USER_API);
        assertEquals(400, mvcResult.getResponse().getStatus());
    }

    @Test
    public void testGetUserById() throws Exception {
        MvcResult mvcResult = mockPostRequest(userMock(), CREATE_USER_API);
        MvcResult loginResult = mockPostRequest(userMock(), LOGIN_API);
        TOKEN = (String) loginResult.getResponse().getHeaderValue("Authorization");
        User user = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                User.class
        );

        mvcResult = mockGetRequest(GET_USER_BY_ID_API + "/" + user.getId());
        User userRetrievedById = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                User.class
        );
        assertNotNull(userRetrievedById);
    }

    @Test
    public void testCreateItemSuccess() throws Exception {
        MvcResult mvcResult = mockPostRequest(createMockItem(), CREATE_ITEM_API);
        Item createdItem = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                Item.class
        );
        assertEquals(createMockItem().getPrice(), createdItem.getPrice());
        assertEquals(createMockItem().getDescription(), createdItem.getDescription());
        assertEquals(createMockItem().getName(), createdItem.getName());
    }

    @Test
    public void testCreateItemFailed() throws Exception {
        Item mockedItem = createMockItem();
        mockedItem.setName(null);
        MvcResult mvcResult = mockPostRequest(mockedItem, CREATE_ITEM_API);
        assertEquals(400, mvcResult.getResponse().getStatus());
        assertNull(mvcResult.getResponse().getContentType());
    }

    @Test
    public void testFindAllItems() throws Exception {
        MvcResult mvcResult = mockGetRequest(GET_ALL_ITEMS_API);
        List<Item> itemList = objectMapper.readValue(
                mvcResult.getResponse().getContentAsByteArray(),
                new TypeReference<List<Item>>() {
                }
        );
        assertFalse(itemList.isEmpty());
    }

    @Test
    public void testFindItemById() throws Exception {
        MvcResult mvcResult = mockGetRequest(GET_ITEM_API + "/1");
        Item retrievedItem = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                Item.class
        );
        assertNotNull(retrievedItem);
        assertEquals(Long.valueOf(1), retrievedItem.getId());
    }


    @Test
    public void testFindItemsByName() throws Exception {
        MvcResult mvcResult = mockGetRequest(GET_ITEM_BY_NAME + "/Round Widget");
        List<Item> itemList = objectMapper.readValue(
                mvcResult.getResponse().getContentAsByteArray(),
                new TypeReference<List<Item>>() {
                }
        );
        assertFalse(itemList.isEmpty());
    }

    @Test
    public void testAddToCart() throws Exception {
        testCreateItemSuccess();
        ModifyCartRequest mockCartRequest = createMockCartRequest();
        MvcResult mvcResult = mockPostRequest(mockCartRequest, ADD_TO_CART_API);
        Cart savedCart = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                Cart.class
        );
        assertNotNull(savedCart.getId());
        assertEquals(userMock().getUsername(), savedCart.getUser().getUsername());
        assertNotNull(savedCart.getTotal());
        assertEquals(createMockItem().getName(), savedCart.getItems().get(0).getName());
        assertEquals(createMockItem().getDescription(), savedCart.getItems().get(0).getDescription());
        assertEquals(createMockItem().getPrice().setScale(0, RoundingMode.UNNECESSARY), savedCart.getItems().get(0).getPrice().setScale(0, RoundingMode.UNNECESSARY));
    }

    @Test
    public void testRemoveFromCart() throws Exception {
        testCreateItemSuccess();
        ModifyCartRequest mockCartRequest = createMockCartRequest();
        MvcResult mvcResult = mockPostRequest(mockCartRequest, REMOVE_FROM_CART_API);
        Cart savedCart = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                Cart.class
        );
        assertNotNull(savedCart.getId());
        assertEquals(userMock().getUsername(), savedCart.getUser().getUsername());
        assertNotNull(savedCart.getTotal());
        assertTrue(savedCart.getItems().isEmpty());
    }

    @Test
    public void testSubmitOrder() throws Exception {
        MvcResult mvcResult = mockPostRequest(null, ORDER_API + "/" + userMock().getUsername());
        UserOrder userOrder = objectMapper.convertValue(
                convertResponseContentToJson(mvcResult),
                UserOrder.class
        );
        assertNotNull(userOrder);
    }

    @Test
    public void testGetHistoryOrder() throws Exception {
        testSubmitOrder();
        MvcResult mvcResult = mockGetRequest(ORDER_HISTORY_API + "/" + userMock().getUsername());
        List<UserOrder> userOrderList = objectMapper.readValue(
                mvcResult.getResponse().getContentAsByteArray(),
                new TypeReference<List<UserOrder>>() {
                }
        );
        assertNotNull(userOrderList);
        assertFalse(userOrderList.isEmpty());
    }
}