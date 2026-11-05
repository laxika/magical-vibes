package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * A permanent changes controllers: "when you gain control of this from another player", "when you lose control of
 * this", "whenever an opponent gains control of a permanent you control". Build it through {@link Triggers}.
 *
 * @param event  who gains or loses control of it, relative to the ability's controller
 * @param object which permanent, relative to the ability's source
 */
public record ControlChangeTrigger(ControlEvent event, ObjectRelation object) implements Trigger {

    public ControlChangeTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(object, "object");
    }

    /** Who gains or loses control of the permanent. */
    public enum ControlEvent {
        /** The ability's controller gains control of it from another player. */
        YOU_GAIN,
        /** The ability's controller loses control of it. */
        YOU_LOSE,
        /** An opponent of the ability's controller gains control of it from them. */
        OPPONENT_GAINS
    }
}
