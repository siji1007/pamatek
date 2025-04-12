package com.example.pamatek;

public class Account {
    public String accountId;
    public String fname;
    public String mname;
    public String lname;
    public String username;
    public String password;

    public String userType;

    // Required empty constructor for Firebase
    public Account() {}

    // Full constructor
    public Account(String accountId, String firstName, String middleInitial, String lastName, String username, String password, String userType) {
        this.accountId = accountId;
        this.fname = firstName;
        this.mname = middleInitial;
        this.lname = lastName;
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getFullName() {
        if (mname != null && !mname.isEmpty()) {
            return fname + " " + mname + " " + lname;
        } else {
            return fname + " " + lname;
        }
    }
}
