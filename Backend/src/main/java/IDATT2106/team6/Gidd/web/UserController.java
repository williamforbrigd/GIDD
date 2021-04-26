package IDATT2106.team6.Gidd.web;

import IDATT2106.team6.Gidd.models.*;
import IDATT2106.team6.Gidd.service.*;
import IDATT2106.team6.Gidd.util.Logger;
import IDATT2106.team6.Gidd.util.PathTwoTokenRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static IDATT2106.team6.Gidd.Constants.*;
import static IDATT2106.team6.Gidd.web.ControllerUtil.*;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/user")
public class UserController {
    private static Logger log = new Logger(UserController.class.toString());
    @Autowired
    private ActivityService activityService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private UserService userService;
    @Autowired
    private TagService tagService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private FriendGroupService friendGroupService;
    @Autowired
    private SimpMessagingTemplate template;

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity getAllUsers() {
        log.debug("Received GetMapping at '/user'");
        try {
            List<User> users = userService.getUsers();
            return ResponseEntity
                    .ok()
                    .body(users.toString());
        } catch (Exception e) {
            log.error("An unexpected error was caught while getting all tags: " +
                    e.getCause() + " with message" + e.getCause().getMessage());
            HashMap<String, String> body = new HashMap<>();
            body.put("error", "something went wrong");

            return ResponseEntity
                    .badRequest()
                    .body(formatJson(body));
        }
    }

    @DeleteMapping(value = "/{userId}/user/{friendId}")
    public ResponseEntity deleteFriend(@PathVariable Integer userId,
                                       @PathVariable Integer friendId) {
        log.debug("Received DeleteMapping to '/user/{userId}/user/{friendId}");
        User user = userService.getUser(userId);
        User friend = userService.getUser(friendId);

        if (friend == null || user == null) {
            log.error("One of the users are null");
            HttpHeaders header = new HttpHeaders();
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "One of the users do not exist");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        if (!userService.deleteFriendship(user, friend)) {
            log.error("The deleting went wrong");
            HttpHeaders header = new HttpHeaders();
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The deleting went wrong");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        log.debug("The friendship was deleted");
        HttpHeaders header = new HttpHeaders();
        header.add("Status", "200 OK");
        header.add("Content-Type", "application/json; charset=UTF-8");

        Map<String, String> body = new HashMap<>();

        body.put("userId", String.valueOf(user.getUserId()));
        body.put("friendId", String.valueOf(friend.getUserId()));
        userService.setPoints(user, (int) (user.getPoints() - ADD_FRIEND_BONUS));
        user.setPoints(user.getPoints() - ADD_FRIEND_BONUS);
        friend.setPoints(friend.getPoints() - ADD_FRIEND_BONUS);

        return ResponseEntity
                .ok()
                .headers(header)
                .body(formatJson(body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable Integer id) {
        //todo return activity-objects and user id's affected by this user being deleted
        // aka the activities this user has created
        log.info("recieved deletemapping to user with id " + id);
        HttpHeaders header = new HttpHeaders();
        boolean result = userService.deleteUser(id);
        Map<String, String> body = new HashMap<>();

        if (result) {
            log.info("deletion successful");
            header.add("Status", "200 OK");
            return ResponseEntity.ok()
                    .headers(header).body(formatJson(body));
        }

        log.error("unable to delete user with id: " + id);
        body.put("error", "deletion failed, are you sure the user with id " + id + " exists?");
        header.add("Status", "400 BAD REQUEST");
        return ResponseEntity.ok()
                .headers(header).body(formatJson(body));
    }

    @DeleteMapping(value = "/{userId}/activity/{activityId}", produces = "application/json")
    public ResponseEntity deleteActivityToUser(@PathVariable Integer userId,
                                               @PathVariable Integer activityId) {
        log.debug(
                "Received DeleteMapping to /user/{userId}/activity/{activityId} with userId being " +
                        userId + " and activityId being " + activityId);
        User user = userService.getUser(userId);
        Activity activity = activityService.findActivity(activityId);

        HttpHeaders header = new HttpHeaders();
        if (user == null) {
            log.error("User is null");
            header.add("Status", "400 REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The user does not exist");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        if (activity == null) {
            log.error("Activity is null");
            header.add("Status", "400 REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The activity does not exist");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        List<ActivityUser> activityUsers = user.getActivities();
        List<Integer> activitiesIds = new ArrayList<>();

        for (ActivityUser au : activityUsers) {
            activitiesIds.add(au.getActivity().getActivityId());
        }

        if (!activitiesIds.contains(activityId)) {
            log.error("There is no connection between user and activity");
            header.add("Status", "400 REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The user is not registered to the activity");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }
        Map<String, String> body = new HashMap<>();

        int activityUserId = userService.getActivityUser(activity, user);

        ActivityUser activityUser = userService.getActivityUserById(activityUserId);
        if (activityUser == null) {
            log.error("There is no connection between user and activity");
            header.add("Status", "400 REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");


            body.put("error", "The user is not registered to the activity");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }
        //if the user being deleted is the owner
        if(activity.getUser().getUserId() == user.getUserId()){
            body.put("error", "cannot delete owner from own activity");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        if (!userService.deleteConnection(activityUser) ||
                !userService.removeActivity(activityUserId, user) ||
                !activityService.removeUserFromActivity(activityUserId, activity)) {
            log.error("An error happened during the deletion");
            header.add("Status", "400 REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");


            body.put("error", "Something went wrong when trying to delete");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        log.debug("The deletion was successful");
        header.add("Status", "200 OK");
        header.add("Content-Type", "application/json; charset=UTF-8");

        body.put("userId", String.valueOf(user.getUserId()));
        body.put("activityId", String.valueOf(activity.getActivityId()));
        //todo bug hvor man kan få minuspoeng dersom man joiner en aktivitet, aktiviteten endres til høyere activity
        // level, og man leaver denne
        userService.setPoints(user, (int) (user.getPoints() -
                JOIN_ACTIVITY_BONUS * MULTIPLIERS[activity.getActivityLevel().ordinal()]));
        return ResponseEntity
                .ok()
                .headers(header)
                .body(formatJson(body));
    }
    @GetMapping("/{userId}/user/{friendId}")
    public ResponseEntity checkFriendship(@PathVariable Integer userId, @PathVariable Integer friendId) {
        log.debug("Received GetMapping to '/user/{userId}/user/{friendId}'");
        User user = userService.getUser(userId);
        User friend = userService.getUser(friendId);

        if (user == null || friend == null) {
            HttpHeaders header = new HttpHeaders();
            log.error("The user or the friend is null");
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The user or the friend does not exist");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        Friendship friendship = userService.checkFriendship(user, friend);

        HttpHeaders header = new HttpHeaders();
        header.add("Status", "200 OK");
        header.add("Content-Type", "application/json; charset=UTF-8");

        log.debug("Friendship is " + friendship);
        HashMap<String, String> body = new HashMap<>();
        body.put("friendship", friendship.toString());

        return ResponseEntity
                .ok()
                .headers(header)
                .body(formatJson(body));
    }

    @GetMapping(value = "/{userId}/user")
    public ResponseEntity getFriends(@PathVariable Integer userId) {
        log.debug("Received GetMapping to '/user/{userId}/user'");
        User user = userService.getUser(userId);

        if (user == null) {
            HttpHeaders header = new HttpHeaders();
            log.error("The user is null");
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The user does not exist");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        ArrayList<User> friends = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"users\":[");

        boolean remove = false;
        for (User u : user.getFriendList()) {
            if (u.getFriendList().contains(user)) {
                friends.add(u);
                stringBuilder.append(u.toJSON());
                stringBuilder.append(",");
                remove = true;
            }
        }

        if(remove) {
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
        }
        stringBuilder.append("]}");

        HttpHeaders header = new HttpHeaders();
        log.debug("Returning friends");
        header.add("Status", "200 OK");
        header.add("Content-Type", "application/json; charset=UTF-8");

        return ResponseEntity
                .ok()
                .headers(header)
                .body(stringBuilder.toString());
    }

    @GetMapping(value = "/{userId}/activity", produces = "application/json")
    public ResponseEntity getAllActivitiesForUser(@PathVariable Integer userId) {
        log.debug("Received GetMapping to '/user/{userId}/activity' with userId " + userId);
        User user = userService.getUser(userId);

        HttpHeaders header = new HttpHeaders();

        if (user == null) {
            log.error("The user is null");
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The user does not exist");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        log.debug("Getting all activities " + user.toString() + " is registered to");
        List<ActivityUser> activityUser = user.getActivities();

        header.add("Status", "200 OK");
        header.add("Content-Type", "application/json; charset=UTF-8");

        StringBuilder sb = new StringBuilder();

        List<Activity> activities =
                activityUser.stream().map(ActivityUser::getActivity).collect(Collectors.toList());

        return ResponseEntity
                .ok()
                .headers(header)
                .body("{\"activities\":" + activities.toString() + "}");
    }

    @GetMapping(value = "/email/{email}", produces = "application/json")
    public ResponseEntity getUser(@PathVariable String email) {
        log.debug("get mapping for email: " + email);
        HttpHeaders header = new HttpHeaders();

        User user = userService.getUser(email);
        if (user != null) {
            return ResponseEntity
                    .ok()
                    .headers(header)
                    .body(user.toJSON());
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("error", "are you sure the user exists?");
        return ResponseEntity
                .badRequest()
                .headers(header)
                .body(formatJson(hashMap));
    }

    @GetMapping(value = "/{userId}", produces = "application/json")
    public ResponseEntity getSingleUser(@PathVariable Integer userId) {
        log.debug("recieved single user get " + userId);
        User user = userService.getUser(userId);
        HttpHeaders header = new HttpHeaders();
        if (user != null) {
            return ResponseEntity
                    .ok()
                    .headers(header)
                    .body(user.toJSON());
        }
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("error", "are you sure the user exists?");
        return ResponseEntity
                .badRequest()
                .headers(header)
                .body(formatJson(hashMap));
    }

    @GetMapping("/{userId}/group")
    public ResponseEntity getGroupsForUser(@PathVariable Integer userId){
        log.debug("Received GetMapping to '/user/{userId}/group'");
        User user = userService.getUser(userId);

        HttpHeaders header = new HttpHeaders();
        HashMap<String, String> body = new HashMap<>();
        if(user == null){
            log.error("The user is null");
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            body.put("error", "The user does not exist");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        List<FriendGroup> allFriendGroups = friendGroupService.getAllFriendGroups();
        ArrayList<FriendGroup> friendGroups = userService.getFriendGroups(userId, allFriendGroups);

        header.add("Status", "200 OK");
        header.add("Content-Type", "application/json; charset=UTF-8");

        return ResponseEntity
                .ok()
                .headers(header)
                .body("{ \"groups\" : " + friendGroups.toString() + "}");
    }

    @PathTwoTokenRequired
    @PutMapping(value = "/some/{id}")
    public ResponseEntity editSomeUser(@RequestBody Map<String, Object> map, @PathVariable Integer id) {
        log.debug("Received request at /user/some/" + id);
        // TODO This method NEEDS to control token once that's possible
        Map<String, String> body = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        if(!parsePhone(map, body)) {
            log.error("Could not parse phoneNumber");

            return ResponseEntity
                    .badRequest()
                    .headers(headers)
                    .body(formatJson(body));
        }

        try{
            log.debug("Attempting to edit user");
            User user = userService.getUser(id);
            log.debug("Found user " + user.toString());
            boolean result = userService.editUser(
                    id,
                    map.get("email").toString(),
                    map.get("newPassword").toString(),
                    map.get("firstName").toString(),
                    map.get("surname").toString(),
                    Integer.parseInt(map.get("phoneNumber").toString()),
                    ActivityLevel.valueOf(map.get("activityLevel").toString()),
                    user.getAuthProvider()
            );

            if (result) {
                log.info("created user");
                body.put("userId", String.valueOf(id));

                return ResponseEntity
                        .ok()
                        .headers(headers)
                        .body(formatJson(body));
            }
        } catch (NullPointerException npe) {
            log.error("a nullpointerexception was caught");
            body.put("error", "invalid parameter");

            return ResponseEntity
                    .badRequest()
                    .body(formatJson(body));
        } catch (Exception e) {
            log.error("An unexpected error was caught while editing user: " + e.getMessage());
            body.put("error", "an unexpected error occurred");

            return ResponseEntity
                    .badRequest()
                    .body(formatJson(body));
        }
        log.error("Could not edit user for some unexpected reason");
        body.put("error", "user could not be edited");

        return ResponseEntity
                .badRequest()
                .headers(headers)
                .body(formatJson(body));
    }

    @PathTwoTokenRequired
    @PutMapping(value = "/{id}")
    public ResponseEntity editUser(@RequestBody Map<String, Object> map, @PathVariable Integer id) {
        log.info("recieved a put mapping for user with id: " + id + " and map " + map.toString());
        Map<String, String> body = new HashMap<>();
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json; charset=UTF-8");

        try {
            if (!userService.login(map.get("email").toString(), map.get("password").toString())) {
                log.debug("Someone tried to edit a user with an invalid email or password ");
                body.put("error", "Invalid Email or Password");
                return ResponseEntity
                        .badRequest()
                        .headers(header)
                        .body(formatJson(body));
            }
        } catch (NullPointerException e) {
            log.error("A NullPointerException was caught while editing user");
            body.put("error", "Invalid Email or Password");
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        if (map.get("newPassword") == null || map.get("newPassword").equals("")) {
            map.put("newPassword", map.get("password"));
        }

        if (map.get("newEmail") == null || map.get("newEmail").equals("")) {
            map.put("newEmail", map.get("email"));
        }

        if (!validateStringMap(map)) {
            log.error(
                    "returning error about null/blank fields in user put mapping " + map.toString());
            body.put("error", "one or more json-fields is null/blank");
            return ResponseEntity.badRequest().body(formatJson(body));
        }

        try {
            Integer.parseInt(map.get("phoneNumber").toString());
        } catch (NumberFormatException e) {
            log.error("phone number cannot be parsed to number " + map.toString());
            body.put("error", "phone number is not numeric");
            return ResponseEntity.badRequest().body(formatJson(body));
        } catch (Exception e) {
            log.error("An unexpected message was caught when parsing phoneNumber: " +
                    e.getMessage() + " local: " + e.getLocalizedMessage());
            body.put("Error", "Something went wrong");
            return ResponseEntity
                    .badRequest()
                    .body(formatJson(body));
        }

        try {
            User oldUser = userService.getUser(id);
            boolean result = userService.editUser(
                    id,
                    map.get("newEmail").toString(),
                    map.get("newPassword").toString(),
                    map.get("firstName").toString(),
                    map.get("surname").toString(),
                    Integer.parseInt(map.get("phoneNumber").toString()),
                    ActivityLevel.valueOf(map.get("activityLevel").toString()),
                    oldUser.getAuthProvider());

            log.info("edited user " + map.toString());
            if (result) {
                log.info("created user");
                header.add("Status", "201 CREATED");

                body.put("id", String.valueOf(id));

                return ResponseEntity.ok()
                        .headers(header)
                        .body(formatJson(body));
            }
        } catch (NullPointerException e) {
            log.debug("A NullPointerException was caught while attempting to edit user");
            body.put("error", "invalid input");
            return ResponseEntity
                    .badRequest()
                    .body(formatJson(body));
        } catch (Exception e) {
            log.debug("An error was caught while attempting to edit user: " +
                    e.getMessage() + " | Local: " + e.getLocalizedMessage());
            body.put("error", "Something went wrong");
            return ResponseEntity.badRequest().body(formatJson(body));
        }

        log.error("User could not be edited, are you sure the user exists");
        header.add("Status", "400 BAD REQUEST");
        body.put("error", "could not edit user are you sure the user exists?");
        return ResponseEntity
                .badRequest()
                .body(formatJson(body));
    }

    @PostMapping(value = "/{userId}/user")
    public ResponseEntity addFriend(@RequestBody HashMap<String, Object> map) {
        log.debug("Adding friend with userId " + map.get("friendId").toString() + " to " +
                map.get("userId").toString());
        User user = userService.getUser(Integer.parseInt(map.get("userId").toString()));
        User friend = userService.getUser(Integer.parseInt(map.get("friendId").toString()));

        if (friend == null || user == null) {
            log.error("One of the users are null");
            HttpHeaders header = new HttpHeaders();
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "One of the users do not exist");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        ArrayList<Integer> friendIds = new ArrayList<>();
        for (User u : user.getFriendList()) {
            friendIds.add(u.getUserId());
        }

        if (friendIds.contains(friend.getUserId())) {
            log.error("There are already a connection between the users");
            HttpHeaders header = new HttpHeaders();
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "The users are already friends");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        if (!userService.updateUser(user, friend)) {
            log.error("Something wrong happened when trying to update");
            HttpHeaders header = new HttpHeaders();
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            Map<String, String> body = new HashMap<>();

            body.put("error", "Something wrong happened when trying to update");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        HttpHeaders header = new HttpHeaders();
        header.add("Status", "200 OK");
        header.add("Content-Type", "application/json; charset=UTF-8");

        Map<String, String> body = new HashMap<>();

        body.put("userId", String.valueOf(user.getUserId()));
        body.put("friendId", String.valueOf(friend.getUserId()));

        userService.setPoints(user, (int) (user.getPoints() + ADD_FRIEND_BONUS));
        return ResponseEntity
                .ok()
                .headers(header)
                .body(formatJson(body));
    }

    @PostMapping(value = "/activity", consumes = "application/json", produces = "application/json")
    public ResponseEntity registerUserToActivity(@RequestBody HashMap<String, Object> map) {
        log.debug("Received PostMapping to '/user/{userId}/activity with userId" +
                Integer.parseInt(map.get("userId").toString()) + " and activityId " +
                Integer.parseInt(map.get("activityId").toString()));

        User user = userService.getUser(Integer.parseInt(map.get("userId").toString()));

        Activity activity =
                activityService.findActivity(Integer.parseInt(map.get("activityId").toString()));

        HttpHeaders header = new HttpHeaders();
        Map<String, String> body = new HashMap<>();

        if (user == null) {
            log.error("User is null");
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");

            body.put("error", "The user does not exist");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        if (activity == null) {
            log.error("Activity is null");
            header.add("Status", "400 BAD REQUEST");
            header.add("Content-Type", "application/json; charset=UTF-8");


            body.put("error", "The activity does not exist");

            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }

        try {
            if (insertUserActivityCoupling(user, activity)) {
                log.debug("The registration was successful");
                header.add("Status", "200 OK");
                header.add("Content-Type", "application/json; charset=UTF-8");

                body.put("userId", String.valueOf(user.getUserId()));
                body.put("activityId", String.valueOf(activity.getActivityId()));

                userService.setPoints(user,
                        (int) (user.getPoints() +
                                JOIN_ACTIVITY_BONUS * MULTIPLIERS[activity.getActivityLevel().ordinal()]));
                return ResponseEntity
                        .ok()
                        .headers(header)
                        .body(formatJson(body));
            } else {
                log.error("Something wrong happened when trying to register the activity to the user");
                header.add("Status", "400 BAD REQUEST");
                header.add("Content-Type", "application/json; charset=UTF-8");


                body.put("error", "Something wrong happened registering the coupling between user and activity");
                return ResponseEntity
                        .badRequest()
                        .headers(header)
                        .body(formatJson(body));

            }
        } catch(IllegalArgumentException e){
            log.error("user is already registered to the activity");
            body.put("error", "user is already registered to the activity: " + e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .headers(header)
                    .body(formatJson(body));
        }
    }

    @PostMapping("")
    public ResponseEntity registerUser(@RequestBody HashMap<String, Object> map) {
        //      TODO Error handling
        //      Return Exception to user
        log.info("recieved postmapping to /user: " + map.toString());
        Map<String, String> body = new HashMap<>();

        if (userService.getUser(map.get("email").toString()) != null) {
            body.put("error", "a user with that email already exists!");

            return ResponseEntity
                    .badRequest()
                    .body(formatJson(body));
        }

        User result = userService.registerUser(
                getRandomID(),
                map.get("email").toString(),
                map.get("password").toString(),
                map.get("firstName").toString(),
                map.get("surname").toString(),
                Integer.parseInt(map.get("phoneNumber").toString()),
                ActivityLevel.valueOf(map.get("activityLevel").toString()),
                Provider.LOCAL);
        log.info("created user with id: " + result.getUserId());
        HttpHeaders header = new HttpHeaders();

        header.add("Content-Type", "application/json; charset=UTF-8");
        log.info("created user " + result.getUserId() + " | " + result.getEmail());
        if (result != null) {
            log.info("created user");
            header.add("Status", "201 CREATED");

            body.put("id", String.valueOf(result.getUserId()));

            return ResponseEntity.ok()
                    .headers(header)
                    .body(formatJson(body));
        }
        log.error("Created user is null, does the user already exist?");
        header.add("Status", "400 BAD REQUEST");
        body.put("error", "Created user is null, does the user already exist?");
        return ResponseEntity.ok()
                .headers(header).body(formatJson(body));
    }

    boolean insertUserActivityCoupling(User user, Activity activity){
        //Legge inn sjekk om den allerede er registrert
        List<ActivityUser> activityUser = user.getActivities();
        ArrayList<Integer> activityIds = new ArrayList<>();
        Timestamp time = new Timestamp(new Date().getTime());

        for (ActivityUser as : activityUser) {
            activityIds.add(as.getActivity().getActivityId());
        }

        if (activityIds.contains(activity.getActivityId())) {
            throw new IllegalArgumentException("The user is already registered to the activity");
        }

        int couplingId = getRandomID();

        //Kalle insert-metode helt til den blir true

        ArrayList<ActivityUser> activityUsers = new ArrayList<>();
        System.out.println("is null " + activityService == null);
        ArrayList<Activity> activities = activityService.getAllActivities();
        System.out.println("activity null " + activities == null);
        for (Activity a : activities) {
            activityUsers.addAll(a.getRegisteredParticipants());
        }

        ArrayList<Integer> couplingIdList = new ArrayList<>();

        for (ActivityUser au : activityUsers) {
            couplingIdList.add(au.getId());
        }

        while (couplingIdList.contains(couplingId)) {
            couplingId = getRandomID();
        }

        if (userService.addUserToActivity(couplingId, activity, user, time)) {
            if (activityService.addUserToActivity(couplingId, activity, user, time)) {
                return true;
            }
            userService.removeActivity(couplingId, user);
            return false;
        }
        return false;
    }

}
