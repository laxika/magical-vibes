package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.DayNight;

import java.util.Objects;

/**
 * The game's day or night designation changed from {@code previous} to {@code current}: the event "whenever day
 * becomes night or night becomes day" abilities trigger on (CR 603.2), which only the game losing one designation and
 * gaining the other is (CR 732.1a), not its first becoming day or night.
 */
public record DayNightEvent(DayNight previous, DayNight current) implements RulesEvent {

    public DayNightEvent {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(current, "current");
    }

    @Override
    public DayNightEvent deepCopy() {
        return this;
    }
}
