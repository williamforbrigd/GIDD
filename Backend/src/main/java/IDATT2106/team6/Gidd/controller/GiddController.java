package IDATT2106.team6.Gidd.controller;

import IDATT2106.team6.Gidd.models.ActivityLevel;
import IDATT2106.team6.Gidd.models.Tag;
import IDATT2106.team6.Gidd.models.User;
import IDATT2106.team6.Gidd.models.Activity;
import IDATT2106.team6.Gidd.service.UserService;
import IDATT2106.team6.Gidd.service.ActivityService;

import java.util.HashMap;
import java.util.Map;

import IDATT2106.team6.Gidd.models.ActivityLevel;
import IDATT2106.team6.Gidd.service.EquipmentService;
import IDATT2106.team6.Gidd.service.UserService;
import java.net.URI;

import java.util.Iterator;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class GiddController {
    @Autowired
    private ActivityService activityService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public ResponseEntity home(){
        activityService.doNothing();
        return ResponseEntity
            .ok()
            .body("hi");
    }

    @PostMapping(value = "/testNewUser", consumes = "application/json", produces = "application/json")
    public ResponseEntity newUserTest(@RequestBody User object){
        userService.testNewUser(object);
        return ResponseEntity
            .created(URI.create("/user/id"))
            .body("hihi");
    }

    @PostMapping(value = "/testNewActivity", consumes = "application/json", produces = "application/json")
    public ResponseEntity newActivityTest(@RequestBody HashMap<String, Object> map){
        Timestamp newTime = Timestamp.valueOf(map.get("time").toString());

        User user = userService.getUser(Integer.parseInt(map.get("userId").toString()));
        int newId = getRandomID();

        //TODO Verify that user-input is valid
        activityService.addActivity(    newId,
            map.get("title").toString(), newTime, (int) map.get("repeat"), user,
            (int) map.get("capacity"), (int) map.get("groupId"), map.get("description").toString(), (byte[])map.get("image"),
            ActivityLevel.valueOf(map.get("activityLevel").toString()), (List<Tag>)map.get("tags"), (Double) map.get("latitude"), (Double) map.get("longitude"));
        return ResponseEntity
            .created(URI.create(String.format("/activity/%d", newId)))
            .body("Insert ResponseBody here");
    }

    private int getRandomID(){
        Random rand = new Random();
        return rand.nextInt();
    }
    @PostMapping("/user")
    public ResponseEntity registerUser(@RequestBody HashMap<String, String> map){

	User result = userService.registerUser(map.get("email").toString(),
              map.get("password").toString(), map.get("firstName").toString(), map.get("surname").toString(),
              Integer.parseInt(map.get("phoneNumber").toString()),
              ActivityLevel.valueOf(map.get("activityLevel").toString()));

        Map<String, String> body = new HashMap<>();
        HttpHeaders header = new HttpHeaders();
        header.add("Access-Control-Allow-Origin", "*");
        header.add("Content-Type", "application/json; charset=UTF-8");
        
		if(result != null){
            body.put("id", String.valueOf(result.getUserId()));
			return ResponseEntity.ok()
            .headers(header)
            .body(formatJson(body)); 
		}

        body.put("id", "error");
		return new ResponseEntity<>(body, HttpStatus.CONFLICT);	
    }

    @PostMapping("/login")
    public ResponseEntity loginUser(@RequestBody HashMap<String, String> map){
		boolean result = userService.login(map.get("email").toString(), map.get("password").toString());
        Map<String, String> body = new HashMap<>();
        HttpHeaders header = new HttpHeaders();
        header.add("Access-Control-Allow-Origin", "*");
        header.add("Content-Type", "application/json; charset=UTF-8");

        if(result){
            body.put("id", String.valueOf(userService.getUser(map.get("email")).getUserId()));
			return ResponseEntity.ok()
            .headers(header)
            .body(formatJson(body));
        }
        body.put("id", "invalid password");
		return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);	
    }

    private String formatJson(Map values){
        String result = "{";
        Iterator it = values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String goose = "";

            //todo very scuffed
            try {
                Integer.parseInt(pair.getValue().toString());
            } catch (Exception e) {
                goose = "\"";
            }

            System.out.println("goose: " + goose + " because " + pair.getValue() instanceof String);
            result += "\"" + pair.getKey() + "\":" + goose + pair.getValue() + goose + ",\n";
            it.remove(); // avoids a ConcurrentModificationException
        }
        //remove trailing comma
        return result.substring(0, result.length() - 2) + "}";
    }
}
