package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * A permanent is tapped (CR 701.26a) or untapped (CR 701.26b). A "becomes tapped" or "becomes untapped" trigger
 * triggers only when a permanent already on the battlefield changes from one state to the other, not when it enters
 * in that state (CR 603.2e). Build it through {@link Triggers}.
 *
 * @param event     what happens to the permanent: it is tapped for mana, becomes tapped (for the first time during
 *                  your turn) or untapped, is tapped by you, is tapped to pay a teamwork cost (CR 702.194a), or you
 *                  untap permanents during your untap step
 * @param relation  how the tapped or untapped permanent relates to the ability's source: {@link ObjectRelation#THIS}
 *                  for "this permanent", {@link ObjectRelation#YOU_CONTROL} for "a permanent you control" (the source
 *                  included), {@link ObjectRelation#OPPONENT_CONTROLS} for "a permanent an opponent controls",
 *                  {@link ObjectRelation#ANY} for "a permanent", {@link ObjectRelation#ENCHANTED} for the permanent
 *                  the source is attached to (enchanted, equipped or fortified)
 * @param filter    what the permanent must be, or {@code null} for any permanent: a land, a creature, a nonland
 *                  permanent, an artifact token. The engine's tap matchers check it in code
 * @param qualifier what the permanent must also be, or {@code null}: "whenever a Merfolk you control becomes tapped"
 *                  is {@code Triggers.permanentYouControlBecomesTapped().withFilter(merfolk)}, "whenever this
 *                  creature becomes tapped while it has a -1/-1 counter on it" {@code
 *                  Triggers.becomesTapped().withFilter(hasMinusOneCounter)}. The matchers judge it from the ability
 *                  ({@code TapTriggers}), so {@link #watchKey()} drops it
 */
public record TapTrigger(TapEvent event, ObjectRelation relation, PermanentPredicate filter,
                         PermanentPredicate qualifier) implements Trigger {

    public TapTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(relation, "relation");
    }

    /** A trigger on any permanent of {@code filter}'s type that relates to the source as {@code relation} says. */
    public TapTrigger(TapEvent event, ObjectRelation relation, PermanentPredicate filter) {
        this(event, relation, filter, null);
    }

    /** A trigger on any permanent that relates to the source as {@code relation} says. */
    public TapTrigger(TapEvent event, ObjectRelation relation) {
        this(event, relation, null, null);
    }

    /**
     * This trigger narrowed to a permanent that also matches {@code filter}, as {@link #qualifier} says: "whenever an
     * artifact you control becomes tapped".
     */
    public TapTrigger withFilter(PermanentPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (qualifier != null) {
            throw new IllegalStateException("This tap trigger already has a filter: " + this);
        }
        return new TapTrigger(event, relation, this.filter, filter);
    }

    /**
     * Whether the tap matchers ({@code TapTriggers}) read this trigger's abilities by another trigger, the one of
     * "a permanent you control becomes tapped", whose matcher judges the rest from the ability: "this permanent
     * becomes tapped" ({@link ObjectRelation#THIS} with {@link TapEvent#BECOMES_TAPPED}) and every
     * {@link TapEvent#BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN}.
     */
    public boolean narrowed() {
        return event == TapEvent.BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN
                || (event == TapEvent.BECOMES_TAPPED && relation == ObjectRelation.THIS);
    }

    /**
     * Whether this is a tap-for-mana trigger read by the matcher of "whenever a player taps a land for mana" or
     * "whenever a player taps a permanent for mana" ({@link #watchKey()}), which judges {@link #relation} from the
     * event: "whenever you tap a land for mana" and "whenever an opponent taps a land / a permanent for mana" (the
     * player who tapped it), "whenever enchanted / fortified land is tapped for mana" (the land the source is attached
     * to) and "whenever you tap this land for mana" (the source itself).
     */
    public boolean judgedTapRelation() {
        if (event != TapEvent.TAPPED_FOR_MANA || relation == ObjectRelation.ANY) {
            return false;
        }
        if (Triggers.LAND.equals(filter)) {
            return relation == ObjectRelation.OPPONENT_CONTROLS || relation == ObjectRelation.YOU_CONTROL
                    || relation == ObjectRelation.ENCHANTED || relation == ObjectRelation.THIS;
        }
        return filter == null && relation == ObjectRelation.OPPONENT_CONTROLS;
    }

    /**
     * The trigger the matchers ({@code TapTriggers}) read this one's abilities by: without its {@link #qualifier}, and,
     * for a {@link #narrowed()} trigger, "a permanent you control becomes tapped" (its {@link #filter} dropped too),
     * and for a {@link #judgedTapRelation()} one "a player taps a land / a permanent for mana".
     * That matcher judges "this permanent", the type, the relation and "for the first time during your turn" from the
     * ability ({@code TapTriggers}).
     */
    @Override
    public Trigger watchKey() {
        if (narrowed()) {
            return new TapTrigger(TapEvent.BECOMES_TAPPED, ObjectRelation.YOU_CONTROL);
        }
        if (judgedTapRelation()) {
            return new TapTrigger(TapEvent.TAPPED_FOR_MANA, ObjectRelation.ANY, filter);
        }
        return qualifier == null ? this : new TapTrigger(event, relation, filter);
    }

    /**
     * What a {@link TapTrigger} triggers on. Each value triggers once for each permanent tapped or untapped, except
     * {@link #YOU_UNTAP_DURING_UNTAP_STEP}, which triggers once for each untap step.
     */
    public enum TapEvent {
        /** A player taps the permanent for mana: activates a mana ability with {T} in its cost. */
        TAPPED_FOR_MANA,
        /** The permanent becomes tapped, whatever taps it. */
        BECOMES_TAPPED,
        /**
         * A creature becomes tapped during its controller's turn for the first time that turn, counting the times it
         * became tapped before the source was on the battlefield: "whenever this creature becomes tapped for the
         * first time during each of your turns".
         */
        BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN,
        /** The ability's controller taps the permanent, as a spell or ability they control instructs. */
        YOU_TAP,
        /** The permanent becomes tapped to pay a spell's teamwork cost (CR 702.194a). */
        TAPPED_TO_PAY_TEAMWORK_COST,
        /** The permanent becomes untapped, in the untap step or by an effect. */
        BECOMES_UNTAPPED,
        /** The ability's controller untaps one or more permanents during their untap step (CR 502.3). */
        YOU_UNTAP_DURING_UNTAP_STEP
    }
}
