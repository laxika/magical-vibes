package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * "When [event], [effects]" made after an exile step of the same resolution and about what that step exiled: a
 * delayed triggered ability on an event (CR 603.7a), the event sibling of {@link RegisterDelayedStepTriggerEffect}.
 * Its effects name those cards as a later step of the resolution would ({@code Subjects.cardsThisWay(EXILE)}), each
 * only in the stay it was exiled to (CR 603.7c). None is made when the step exiled nothing, or when its event can no
 * longer happen.
 *
 * @param event   what it waits for
 * @param effects what it does
 */
public record RegisterDelayedEventTriggerEffect(Event event, List<CardEffect> effects) implements CardEffect {

    /** The events a delayed ability about exiled cards waits for. */
    public enum Event {
        /** "When this artifact leaves the battlefield": the next time, whenever that is (CR 603.7b). */
        SOURCE_LEAVES,
        /**
         * "When an exiled card enters under your control this way": the next time a card an exile until the source
         * leaves the battlefield returned enters under the ability's controller's control; the entering permanent is
         * {@code Subjects.triggering()}.
         */
        EXILED_CARD_RETURNS,
        /**
         * "When you cast a spell this way": the next time this turn the ability's controller casts one of the exiled
         * cards, which is {@code Subjects.triggeringCard()}.
         */
        YOU_CAST_EXILED_CARD
    }

    public RegisterDelayedEventTriggerEffect {
        Objects.requireNonNull(event, "event");
        effects = List.copyOf(effects);
    }

    /** "When this [permanent] leaves the battlefield, [effects]." */
    public static RegisterDelayedEventTriggerEffect whenSourceLeaves(CardEffect... effects) {
        return new RegisterDelayedEventTriggerEffect(Event.SOURCE_LEAVES, List.of(effects));
    }

    /** "When an exiled card enters under your control this way, [effects]." */
    public static RegisterDelayedEventTriggerEffect whenExiledCardReturns(CardEffect... effects) {
        return new RegisterDelayedEventTriggerEffect(Event.EXILED_CARD_RETURNS, List.of(effects));
    }

    /** "When you cast a spell this way, [effects]." */
    public static RegisterDelayedEventTriggerEffect whenYouCastExiledCard(CardEffect... effects) {
        return new RegisterDelayedEventTriggerEffect(Event.YOU_CAST_EXILED_CARD, List.of(effects));
    }
}
