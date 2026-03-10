package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record MayAbilityMessage(MessageType type, String prompt, boolean canPay, String manaCost) {

    public MayAbilityMessage(String prompt, boolean canPay, String manaCost) {
        this(MessageType.MAY_ABILITY_CHOICE, prompt, canPay, manaCost);
    }
}
