package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {

    private MessageType type;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public static RegisterResponse success() {
        return new RegisterResponse(MessageType.REGISTER_SUCCESS, "Registration successful");
    }

    public static RegisterResponse failure(String message) {
        return new RegisterResponse(MessageType.REGISTER_FAILURE, message);
    }
}
