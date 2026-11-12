package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;

import java.util.Objects;
import java.util.UUID;

/**
 * A triggered ability whose trigger event has occurred, as its collector hands it to the stack door
 * ({@link TriggeredAbilityStackService#put}): the ability whole, its source and controller, and the record of the
 * event. The collector has judged the trigger's filters and event condition; the door does everything else.
 *
 * @param ability          the ability, with its options and its effects as the card wrote them
 * @param sourceCard       the card whose ability it is
 * @param sourcePermanent  the permanent whose ability it is (CR 113.7), or {@code null} for an ability of a card in
 *                         another zone
 * @param controllerId     the ability's controller (CR 113.8)
 * @param description      what the stack entry is called
 * @param ifContext        the context the intervening "if" is judged in as the ability triggers (CR 603.4)
 * @param record           stamps what the event was on the stack entry
 */
public record TriggeredAbilityRequest(TriggeredAbility ability, Card sourceCard, Permanent sourcePermanent,
                                      UUID controllerId, String description, ConditionContext ifContext,
                                      TriggerRecord record) {

    public TriggeredAbilityRequest {
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(sourceCard, "sourceCard");
        Objects.requireNonNull(controllerId, "controllerId");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(ifContext, "ifContext");
        Objects.requireNonNull(record, "record");
    }

    /** The id of {@link #sourcePermanent}, or {@code null}. */
    public UUID sourcePermanentId() {
        return sourcePermanent == null ? null : sourcePermanent.getId();
    }

    /**
     * What a collector knows of the event: the objects and players it is about and the values it carries, stamped on
     * the ability's stack entry whatever the ability's effects are. A non-targeting {@code targetId} the record sets is
     * the object the event names ("that creature", CR 115.10a).
     */
    @FunctionalInterface
    public interface TriggerRecord {

        /** Records nothing beyond the source and controller. */
        TriggerRecord NONE = entry -> entry.setNonTargeting(true);

        /** Stamps the event on {@code entry}. */
        void stamp(StackEntry entry);
    }
}
