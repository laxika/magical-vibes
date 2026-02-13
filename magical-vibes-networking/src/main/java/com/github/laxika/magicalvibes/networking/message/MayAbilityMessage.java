package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record MayAbilityMessage(MessageType type, String prompt) {

    public MayAbilityMessage(String prompt) {
        this(MessageType.MAY_ABILITY_CHOICE, prompt);
    }
}
