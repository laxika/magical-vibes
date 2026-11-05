package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.StateTrigger;

import java.util.List;

/**
 * A state-triggered ability (CR 603.8) in the form the state-trigger reader takes it: a card's state trigger is
 * authored as a {@link StateTrigger} game state and the ability's effects ({@code Triggers.state(predicate)}, …), and
 * the state-trigger reader ({@code MiscAbilityReader}) reads the ability as one of these, with no description; a
 * static ability that grants a state trigger (Olivia, Crimson Bride) still writes one. When the condition is met and the ability is not
 * already on the stack, the {@code effects} are put onto the stack as a triggered ability. The
 * trigger won't fire again while it's on the stack; once resolved, countered, or otherwise
 * removed, it can fire again.
 *
 * <p>The condition is expressed either as a free-form {@code predicate} over the whole game state
 * or, when it is purely a property of the source permanent, as a {@code sourcePredicate}. The
 * latter is evaluated through the CR 613 layer-aware predicate evaluator, so keyword/color/type
 * grants from static effects are visible ("When this creature has flying, sacrifice it" —
 * Floodgate). When both are supplied, both conditions must be met.</p>
 *
 * @param predicate       condition to check after state-based actions, or {@code null}
 * @param sourcePredicate layer-aware condition on the source permanent, or {@code null}
 * @param battlefieldPredicate layer-aware predicate counted across all battlefields, or {@code null}
 * @param maximumBattlefieldMatches maximum matching permanents for the condition to be true
 * @param minimumBattlefieldMatches minimum matching permanents for the condition to be true
 * @param referencedPermanentPredicate selects a non-targeting permanent reference when the
 *                                     trigger fires, or {@code null}
 * @param effects         effects to put on the stack when triggered
 * @param description     log/stack description for the triggered ability, or {@code null} for "[card name]'s
 *                        state-triggered ability"
 *
 * <p>The optional {@code referencedPermanentPredicate} is evaluated across all battlefields and
 * stores the first matching permanent as a non-targeting reference on the triggered stack entry.</p>
 */
public record StateTriggerEffect(
        StateTriggerPredicate predicate,
        PermanentPredicate sourcePredicate,
        PermanentPredicate battlefieldPredicate,
        int maximumBattlefieldMatches,
        int minimumBattlefieldMatches,
        PermanentPredicate referencedPermanentPredicate,
        List<CardEffect> effects,
        String description
) implements CardEffect {

    /** A state trigger's ability as its reader reads it: {@code trigger}'s game state and the ability's effects. */
    public static StateTriggerEffect of(StateTrigger trigger, List<CardEffect> effects) {
        return new StateTriggerEffect(trigger.predicate(), trigger.sourcePredicate(), trigger.battlefieldPredicate(),
                trigger.maximumBattlefieldMatches(), trigger.minimumBattlefieldMatches(),
                trigger.referencedPermanentPredicate(), List.copyOf(effects), null);
    }

    public StateTriggerEffect(StateTriggerPredicate predicate,
                              PermanentPredicate sourcePredicate,
                              PermanentPredicate battlefieldPredicate,
                              int maximumBattlefieldMatches,
                              PermanentPredicate referencedPermanentPredicate,
                              List<CardEffect> effects,
                              String description) {
        this(predicate, sourcePredicate, battlefieldPredicate, maximumBattlefieldMatches, 0,
                referencedPermanentPredicate, effects, description);
    }

    public StateTriggerEffect(StateTriggerPredicate predicate, List<CardEffect> effects, String description) {
        this(predicate, null, null, 0, 0, null, effects, description);
    }

    public StateTriggerEffect(StateTriggerPredicate predicate,
                              PermanentPredicate referencedPermanentPredicate,
                              List<CardEffect> effects, String description) {
        this(predicate, null, null, 0, 0, referencedPermanentPredicate, effects, description);
    }

    public StateTriggerEffect(PermanentPredicate sourcePredicate, List<CardEffect> effects, String description) {
        this(null, sourcePredicate, null, 0, 0, null, effects, description);
    }

    public static StateTriggerEffect whenBattlefieldHasAtMost(
            int maximumMatches, PermanentPredicate predicate, List<CardEffect> effects, String description) {
        return new StateTriggerEffect(null, null, predicate, maximumMatches, 0, null, effects, description);
    }

    public static StateTriggerEffect whenBattlefieldHasAtLeast(
            int minimumMatches, PermanentPredicate predicate, List<CardEffect> effects, String description) {
        return new StateTriggerEffect(null, null, predicate, Integer.MAX_VALUE, minimumMatches,
                null, effects, description);
    }
}
