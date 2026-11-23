package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * "When [event], [effects]": a delayed triggered ability on an event (CR 603.7a), the event sibling of
 * {@link RegisterDelayedStepTriggerEffect}. It is made about one of two things:
 * <ul>
 *   <li>what an exile step of the same resolution exiled ({@code permanent} {@code null}): its effects name those cards
 *       as a later step of the resolution would ({@code Subjects.cardsThisWay(EXILE)}), each only in the stay it was
 *       exiled to (CR 603.7c); none is made when the step exiled nothing;</li>
 *   <li>"that creature", the permanent {@code permanent} names as this resolves ({@link #aboutWhenSourceLeaves}, …):
 *       "when this artifact leaves the battlefield this turn, destroy that creature". Its effects name that permanent
 *       as {@code Subjects.triggering()}, and don't affect it once it has left the battlefield, even if it came back
 *       (CR 603.7c, 400.7); none is made when {@code permanent} names none.</li>
 *   <li>the leaving of the permanent {@code permanent} names ({@link Event#PERMANENT_LEAVES}, {@link #whenLeavesThisTurn}):
 *       "when that creature leaves the battlefield this turn, sacrifice this creature". It is about that permanent, and
 *       its effects' "this" is the ability's source (CR 603.7e); none is made when {@code permanent} names none.</li>
 * </ul>
 * None is made when its event can no longer happen: the source left the battlefield before this resolved (CR 603.7a).
 * One whose event already happened before this resolved ("becomes untapped") waits for the next time (CR 603.7a).
 *
 * @param event     what it waits for
 * @param effects   what it does
 * @param permanent "that creature" it is about, as this resolves: the target another effect of the ability declares
 *                  ({@code Subjects.target()}; this one declares none), or the permanent an earlier step put onto the
 *                  battlefield ({@code Subjects.returned()}); {@code null} for one about the exile step
 * @param thisTurn  whether it has the stated duration "this turn": it ends with the turn if it hasn't triggered;
 *                  without it, it triggers only the next time its event occurs (CR 603.7b)
 */
public record RegisterDelayedEventTriggerEffect(Event event, List<CardEffect> effects, PermanentSubject permanent,
                                                boolean thisTurn) implements CardEffect {

    /** The events a delayed ability waits for. */
    public enum Event {
        /** "When this artifact leaves the battlefield": the next time, whenever that is (CR 603.7b). */
        SOURCE_LEAVES,
        /**
         * "When this creature leaves the battlefield or becomes untapped", whichever happens first (Merieke Ri Berit,
         * Giant Oyster). Only for one about a permanent.
         */
        SOURCE_LEAVES_OR_UNTAPS,
        /**
         * "When this creature becomes untapped or you lose control of this creature", whichever happens first; its
         * leaving the battlefield loses you control of it too (Coffin Queen). Only for one about a permanent.
         */
        SOURCE_UNTAPS_OR_CONTROL_LOST,
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
        YOU_CAST_EXILED_CARD,
        /**
         * "When that creature leaves the battlefield": the next time the permanent the ability is about leaves (Kjeldoran
         * Elite Guard, Runesword). Only for one about a permanent; its effects' "this" is the ability's source.
         */
        PERMANENT_LEAVES;

        /** Whether this is an event of the ability's source permanent. */
        public boolean ofSource() {
            return this == SOURCE_LEAVES || this == SOURCE_LEAVES_OR_UNTAPS || this == SOURCE_UNTAPS_OR_CONTROL_LOST;
        }
    }

    public RegisterDelayedEventTriggerEffect {
        Objects.requireNonNull(event, "event");
        effects = List.copyOf(effects);
        if (permanent != null && !event.ofSource() && event != Event.PERMANENT_LEAVES) {
            throw new IllegalArgumentException("Only an ability on an event of its source is about a permanent: "
                    + event);
        }
        if (permanent == null && (event == Event.SOURCE_LEAVES_OR_UNTAPS
                || event == Event.SOURCE_UNTAPS_OR_CONTROL_LOST || event == Event.PERMANENT_LEAVES)) {
            throw new IllegalArgumentException("An ability on an untap or a permanent's leaving is about a permanent: "
                    + event);
        }
        if (thisTurn && permanent == null) {
            throw new IllegalArgumentException("Only an ability about a permanent lasts this turn: " + event);
        }
    }

    /** {@code effects} on {@code event}, about the exile step. */
    public RegisterDelayedEventTriggerEffect(Event event, List<CardEffect> effects) {
        this(event, effects, null, false);
    }

    /** "When this [permanent] leaves the battlefield, [effects]", about the exile step. */
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

    /**
     * "When this [permanent] leaves the battlefield this turn, [effects] that creature" (War Barge, Phantasmal Mount),
     * about the permanent {@code permanent} names.
     */
    public static RegisterDelayedEventTriggerEffect aboutWhenSourceLeavesThisTurn(PermanentSubject permanent,
                                                                                 CardEffect... effects) {
        return new RegisterDelayedEventTriggerEffect(Event.SOURCE_LEAVES, List.of(effects),
                Objects.requireNonNull(permanent, "permanent"), true);
    }

    /**
     * "When that creature leaves the battlefield this turn, [effects]" (Kjeldoran Elite Guard's "sacrifice this
     * creature"), about the permanent {@code permanent} names as this resolves.
     */
    public static RegisterDelayedEventTriggerEffect whenLeavesThisTurn(PermanentSubject permanent,
                                                                       CardEffect... effects) {
        return new RegisterDelayedEventTriggerEffect(Event.PERMANENT_LEAVES, List.of(effects),
                Objects.requireNonNull(permanent, "permanent"), true);
    }

    /**
     * "When this creature leaves the battlefield or becomes untapped, [effects] that creature" (Merieke Ri Berit,
     * Giant Oyster), about the permanent {@code permanent} names, with no stated duration.
     */
    public static RegisterDelayedEventTriggerEffect aboutWhenSourceLeavesOrUntaps(PermanentSubject permanent,
                                                                                 CardEffect... effects) {
        return new RegisterDelayedEventTriggerEffect(Event.SOURCE_LEAVES_OR_UNTAPS, List.of(effects),
                Objects.requireNonNull(permanent, "permanent"), false);
    }

    /**
     * "When this creature becomes untapped or you lose control of this creature, [effects] that creature" (Coffin
     * Queen), about the permanent {@code permanent} names, with no stated duration.
     */
    public static RegisterDelayedEventTriggerEffect aboutWhenSourceUntapsOrControlLost(PermanentSubject permanent,
                                                                                      CardEffect... effects) {
        return new RegisterDelayedEventTriggerEffect(Event.SOURCE_UNTAPS_OR_CONTROL_LOST, List.of(effects),
                Objects.requireNonNull(permanent, "permanent"), false);
    }
}
