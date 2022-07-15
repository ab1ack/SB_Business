package com.mycompany.sb_business;

public class LoginSession {
    public static String userID;
    public static String userFirst;
    public static String userLast;
    public static boolean isLoggedIn = false;
    public static void logout () {
        userID = null;
        userFirst = null;
        userFirst = null;
        isLoggedIn = false;
    }
}
