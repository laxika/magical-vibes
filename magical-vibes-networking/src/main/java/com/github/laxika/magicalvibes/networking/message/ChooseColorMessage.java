package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ChooseColorMessage(MessageType type, List<String> colors, String prompt) {

    public ChooseColorMessage(List<String> colors, String prompt) {
        this(MessageType.CHOOSE_COLOR, colors, prompt);
    }
}
