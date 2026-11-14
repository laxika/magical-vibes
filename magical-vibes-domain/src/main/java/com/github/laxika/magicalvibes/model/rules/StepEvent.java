package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.trigger.StepTrigger;

import java.util.Objects;
import java.util.UUID;

/**
 * A step or phase began: the event "at the beginning of" abilities trigger on (CR 603.2b). The turn's second main
 * phase is an event of its own beside the postcombat (or precombat) main phase it is.
 *
 * @param moment         the step or phase that began
 * @param activePlayerId the player whose step or phase it is
 */
public record StepEvent(StepTrigger.Moment moment, UUID activePlayerId) implements RulesEvent {

    public StepEvent {
        Objects.requireNonNull(moment, "moment");
        Objects.requireNonNull(activePlayerId, "activePlayerId");
    }

    @Override
    public StepEvent deepCopy() {
        return this;
    }
}
