package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.UUID;

public record StackEntryView(
        StackEntryType entryType,
        CardView card,
        UUID controllerId,
        String description
) {

    public static StackEntryView from(StackEntry entry) {
        return new StackEntryView(
                entry.getEntryType(),
                CardView.from(entry.getCard()),
                entry.getControllerId(),
                entry.getDescription()
        );
    }
}
