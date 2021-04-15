package IDATT2106.team6.Gidd.service;

import IDATT2106.team6.Gidd.models.Activity;
import IDATT2106.team6.Gidd.models.User;
import IDATT2106.team6.Gidd.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class UserService {
    @Autowired
    UserRepo repo;

    public void testNewUser(User user){
        repo.addUser(user);
    }

    public User getUser(int userId){
        return repo.findUser(userId);
    }

    public boolean addUserToActivity(int id, Activity activity, User user, Timestamp time){
        return this.repo.addUserToActivity(id, activity, user, time);
    }
}
