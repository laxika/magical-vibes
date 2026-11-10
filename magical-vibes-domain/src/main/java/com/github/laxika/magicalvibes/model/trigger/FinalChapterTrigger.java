package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * The final chapter ability of a Saga you control (CR 715.2e) triggers or resolves: "Whenever the final chapter ability
 * of a Saga you control resolves". A Saga's final chapter ability is the one with its final chapter number, the
 * greatest among its chapter abilities (CR 715.2d). Build it through {@link Triggers}.
 *
 * @param event what happens to the final chapter ability
 */
public record FinalChapterTrigger(FinalChapterEvent event) implements Trigger {

    public FinalChapterTrigger {
        Objects.requireNonNull(event, "event");
    }

    /** What happens to the final chapter ability of a Saga you control. */
    public enum FinalChapterEvent {
        /** It triggers: it is put on the stack, before its targets are chosen. */
        TRIGGERS,
        /** It finishes resolving. */
        RESOLVES
    }
}
