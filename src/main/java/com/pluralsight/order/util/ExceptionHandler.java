package com.pluralsight.order.util;

import java.sql.SQLException;

/**
 * Utility class to handle exceptions
 */
public class ExceptionHandler {

    /**
     * Method to extract and print information from a SQLException
     * @param sqlException Exception from which information will be extracted
     */
    public static void handleException(SQLException sqlException) {
        int errorCode = sqlException.getErrorCode();
        String sqlState = sqlException.getSQLState();
        String message = sqlException.getMessage();
        System.out.println( errorCode + sqlState + message );
        sqlException.printStackTrace();
    }
}
