package com.mycompany.sb_business;

import javafx.scene.control.Alert;

public class Logout {
    public static void logout(LoginController loginController) {
        LoginSession.isLoggedIn = false;
    }
}
