package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * A permanent is tapped (CR 701.26a) or untapped (CR 701.26b). A "becomes tapped" or "becomes untapped" trigger
 * triggers only when a permanent already on the battlefield changes from one state to the other, not when it enters
 * in that state (CR 603.2e). Build it through {@link Triggers}.
 *
 * @param event    what happens to the permanent: it is tapped for mana, becomes tapped or untapped, is tapped by you,
 *                 is tapped to pay a teamwork cost (CR 702.194a), or you untap permanents during your untap step
 * @param relation how the tapped or untapped permanent relates to the ability's source: {@link ObjectRelation#THIS}
 *                 for "this permanent", {@link ObjectRelation#YOU_CONTROL} for "a permanent you control" (the source
 *                 included), {@link ObjectRelation#OPPONENT_CONTROLS} for "a permanent an opponent controls",
 *                 {@link ObjectRelation#ANY} for "a permanent", {@link ObjectRelation#ENCHANTED} for the permanent
 *                 the source is attached to (enchanted, equipped or fortified)
 * @param filter   what the permanent must be, or {@code null} for any permanent: a land, a creature, a nonland
 *                 permanent, an artifact token. The engine's tap collectors check it in code
 */
public record TapTrigger(TapEvent event, ObjectRelation relation, PermanentPredicate filter) implements Trigger {

    public TapTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(relation, "relation");
    }

    /** A trigger on any permanent that relates to the source as {@code relation} says. */
    public TapTrigger(TapEvent event, ObjectRelation relation) {
        this(event, relation, null);
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
