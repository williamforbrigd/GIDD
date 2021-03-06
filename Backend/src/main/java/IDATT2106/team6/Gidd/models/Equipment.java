package IDATT2106.team6.Gidd.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Equipment {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "equipment_id")
    private int equipmentId;
    private String description;
    @OneToMany(mappedBy = "Equipment")
    private List<ActivityEquipment> activities;

    public Equipment(String description){
        this.description = description;
        this.activities = new ArrayList<>();
    }

    public Equipment(){}

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ActivityEquipment> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityEquipment> activities) {
        this.activities = activities;
    }

    public void addActivityToEquipment(ActivityEquipment activityEquipment){
        this.activities.add(activityEquipment);
    }

    @Override
    public String toString() {
        return "{\n " +
                    "\"equipmentID\": " + equipmentId + "\n" +
                    "\"description\": \"" + description.trim() + "\"\n" +
                "}";
    }
}
