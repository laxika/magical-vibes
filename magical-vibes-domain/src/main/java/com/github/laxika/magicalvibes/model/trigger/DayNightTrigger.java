package com.github.laxika.magicalvibes.model.trigger;

/**
 * "Whenever day becomes night or night becomes day": the game loses one of the day and night designations and gains
 * the other (CR 731.1a). It doesn't trigger when it first becomes day or night. Build it through {@link Triggers}.
 */
public record DayNightTrigger() implements Trigger {
}
