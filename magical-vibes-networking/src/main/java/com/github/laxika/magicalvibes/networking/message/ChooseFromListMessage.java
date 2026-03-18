package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ChooseFromListMessage(MessageType type, List<String> options, String prompt) {

    public ChooseFromListMessage(List<String> options, String prompt) {
        this(MessageType.CHOOSE_FROM_LIST, options, prompt);
    }
}
