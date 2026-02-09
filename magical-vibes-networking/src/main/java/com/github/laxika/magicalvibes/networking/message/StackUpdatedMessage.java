package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record StackUpdatedMessage(MessageType type, List<StackEntry> stack) {
    public StackUpdatedMessage(List<StackEntry> stack) {
        this(MessageType.STACK_UPDATED, stack);
    }
}
