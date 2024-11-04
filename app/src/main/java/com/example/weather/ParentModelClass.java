package com.example.weather;

import java.util.List;

public class ParentModelClass {

    String date;
    List<ChildModelClass> childModelClassList;

    public ParentModelClass(String date, List<ChildModelClass> childModelClassList) {
        this.date = date;
        this.childModelClassList = childModelClassList;
    }

    public String getDate() {
        return date;
    }
}
