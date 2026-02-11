package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;

import java.util.List;

public record StackUpdatedMessage(MessageType type, List<StackEntryView> stack) {
    public StackUpdatedMessage(List<StackEntryView> stack) {
        this(MessageType.STACK_UPDATED, stack);
    }
}
