package org.example.teams_registration.config;

import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<JsonObject> handleSQLException(SQLException e) {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.addProperty("message", "Internal error while accessing the database. Please try again later.");
        errorResponse.addProperty("details", e.getMessage());
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonObject> handleGenericException(Exception e) {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.addProperty("message", "An unexpected error occurred. Please contact support.");

        errorResponse.addProperty("details", e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<JsonObject> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("code", HttpStatus.BAD_REQUEST.value());
        errorResponse.addProperty("message", "Required parameter is missing: " + e.getParameterName() + ".");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
