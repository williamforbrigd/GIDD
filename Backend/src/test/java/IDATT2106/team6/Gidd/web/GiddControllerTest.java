package IDATT2106.team6.Gidd.web;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import IDATT2106.team6.Gidd.GiddApplication;
import IDATT2106.team6.Gidd.models.Activity;
import IDATT2106.team6.Gidd.models.ActivityLevel;
import IDATT2106.team6.Gidd.models.Provider;
import IDATT2106.team6.Gidd.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RunWith(SpringRunner.class) // JUnit
@SpringBootTest(webEnvironment = MOCK, classes = GiddApplication.class) // Spring
@AutoConfigureMockMvc // Trengs for å kunne autowire MockMvc
public class GiddControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GiddController controller;

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private User user5;
    private Activity activity1;

    @Before
    public void initialize() throws Exception {
        System.out.println("Initalizing tests");

        user1 = new User(11, "1@1", "pass1", "Olav", "Skundeberg", 123,
        ActivityLevel.HIGH,
        Provider.LOCAL);

        user2 = new User(22, "2@2", "pass2", "Ole", "Christian", 1232,
        ActivityLevel.HIGH,
        Provider.LOCAL);

        user3 = new User(33, "3@3", "pass3", "Hans jakob", "Matte", 1233,
        ActivityLevel.HIGH,
        Provider.LOCAL);

        user4 = new User(44, "4@4", "pass4", "Jonas", "Støhre", 1234,
        ActivityLevel.HIGH, Provider.LOCAL);

        user5 = new User(55, "5@5", "pass5", "Erna", "Solberg", 1235,
        ActivityLevel.HIGH, Provider.LOCAL);

        activity1 = new Activity(121, "skrive tester",
        new Timestamp(2001, 9, 11, 9, 11, 59, 5 ),
        0, user1, 50, 5, "det som du gjør nå", new byte[]{-5},
        ActivityLevel.HIGH, null, 0.001, 0.005, null);
    }

    @BeforeEach
    public void beforeEach(){
        System.out.println("Beginning a test!\n");



    }

  //  @Test
    void getActivity() throws Exception {
        mockMvc.perform(get("/activity")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.activity", Matchers.greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(1)
    public void registerUserTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
            .post("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(user1.toJSON()))
            .andExpect(MockMvcResultMatchers.status()
                .isOk())
            .andExpect(MockMvcResultMatchers.content()
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Order(2)
    @Test
    public void loginTest() throws Exception {
        //login user from order 1
/*        mockMvc.perform(MockMvcRequestBuilders
            .post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(user1.toJSON()))
            .andExpect(MockMvcResultMatchers.status()
                .isOk())
            .andExpect(jsonPath("$"), Matchers.con)*/
    }

    @Order(3)
    @Test
    public void newActivityTest() {
        System.out.println("test 3");
        //create new activity
    }

    @Order(4)
    @Test
    public void editActivityTest() {
        //edit activity from order 3
        System.out.println("test 4");
    }

    @Order(5)
    @Test
    public void getSingleActivityTest() {
        //get activity from order 4 and threee
        System.out.println("test 5");
    }

    @Order(6)
    @Test
    public void registerUserToActivity() {
        // register user 2
        System.out.println("test 6");
    }

    @Order(7)
    @Test
    public void getAllActivitiesForUserTest() throws Exception{
        //for both user 1 and two
        System.out.println("test 7");
        mockMvc.perform(get("/user/" + user1.getUserId() + "/activity")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.activities").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.activities").isNotEmpty());

        mockMvc.perform(get("/user/" + user2.getUserId() + "/activity")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.activities").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.activities").isNotEmpty());

        mockMvc.perform(get("/user/" + user3.getUserId() + "/activity")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.activities").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.activities").isEmpty());
    }

    @Order(8)
    @Test
    public void getAllUsersFromActivityTest() throws Exception{
        //create new user, add to activity and check order
        System.out.println("test 8");

        String id = mockMvc.perform(post("/user").content("{" +
            "\"email\":\"ikhovidn@email.com\"," +
            "\"password\":\"123\"," +
            "\"firstName\":\"ingebrigt\"," +
            "\"surname\":\"Hovidn\"," +
            "\"phoneNumber\":\"123\"," +
            "\"activityLevel\":\"LOW\"" +
        "}")).andReturn().getResponse().getContentAsString();


        JSONObject json  = (JSONObject) org.skyscreamer.jsonassert.JSONParser.parseJSON(id);

        mockMvc.perform(post("/user/activity").contentType(MediaType.APPLICATION_JSON)
        .content("{"+
                    "\"userId\":" + "\"" + json.getAsNumber("id") + "\"" +
                    "\"activityId\":" + "\"" + activity1.getActivityId() + "\"" +
                "}"
        ));

        String order = mockMvc.perform(get("/activity/" + activity1.getActivityId() + "/user")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user").exists())
        .andReturn().getResponse().getContentAsString();

        JSONObject jsonOrder  = (JSONObject) org.skyscreamer.jsonassert.JSONParser.parseJSON(order);

        String csv = (String) jsonOrder.get("user");


        //order is supposed to be user1 -> user2 -> user3
        assertEquals(Integer.parseInt(csv.split(",")[0]), user1.getUserId());
        assertEquals(Integer.parseInt(csv.split(",")[1]), user2.getUserId());
        assertEquals(Integer.parseInt(csv.split(",")[2]), user3.getUserId());
    }

    @Order(9)
    @Test
    public void deleteActivityToUserTest() throws Exception{
        //remove activity from user 3
        System.out.println("test 9");

        mockMvc.perform(MockMvcRequestBuilders
        .delete("/user/" + user1.getUserId() + "/activity/" + activity1.getActivityId()))
        .andExpect(status().isOk());

        String order = mockMvc.perform(get("/activity/" + activity1.getActivityId() + "/user")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user").exists())
        .andReturn().getResponse().getContentAsString();

        JSONObject jsonOrder  = (JSONObject) org.skyscreamer.jsonassert.JSONParser.parseJSON(order);

        String csv = (String) jsonOrder.get("user");

        //order is supposed to be user1 -> user2
        assertEquals(csv.split(",").length, 2);
        assertEquals(Integer.parseInt(csv.split(",")[0]), user1.getUserId());
        assertEquals(Integer.parseInt(csv.split(",")[1]), user2.getUserId());

    }

    @Order(10)
    @Test
    public void deleteUserTest() throws Exception{
        System.out.println("test 10");

        String response = mockMvc.perform(MockMvcRequestBuilders
        .delete("/user/" + user1.getUserId() + "/activity/" + activity1.getActivityId()))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JSONObject jsonOrder  = (JSONObject) org.skyscreamer.jsonassert.JSONParser.parseJSON(response);

        String csv = (String) jsonOrder.get("user");
        //todo fill out after the todo in the delete mapping is complete
        // create one user
        // create new activity with owner the user from order 8
        // sign up new user to this activity
        // delete user from order 8
        // check that user is deleted and that the new user is returned
    }

    @Order(11)
    @Test
    public void deleteActivityTest(){
        System.out.println("test 11");
        //delete activity and check that users are returned
    }

    @Order(12)
    @Test
    public void editUserTest(){
        //edit user2
    }


    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}