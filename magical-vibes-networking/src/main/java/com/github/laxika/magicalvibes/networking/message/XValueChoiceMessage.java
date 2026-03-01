package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record XValueChoiceMessage(MessageType type, String prompt, int maxValue, String cardName) {

    public XValueChoiceMessage(String prompt, int maxValue, String cardName) {
        this(MessageType.X_VALUE_CHOICE, prompt, maxValue, cardName);
    }
}
