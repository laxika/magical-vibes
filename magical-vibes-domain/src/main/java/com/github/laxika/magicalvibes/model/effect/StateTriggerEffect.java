package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * A state-triggered ability (MTG rule 603.8). When the condition is met and the ability is not
 * already on the stack, the {@code effects} are put onto the stack as a triggered ability. The
 * trigger won't fire again while it's on the stack; once resolved, countered, or otherwise
 * removed, it can fire again.
 *
 * <p>The condition is expressed either as a free-form {@code predicate} over the whole game state
 * or, when it is purely a property of the source permanent, as a {@code sourcePredicate}. The
 * latter is evaluated through the CR 613 layer-aware predicate evaluator, so keyword/color/type
 * grants from static effects are visible ("When this creature has flying, sacrifice it" —
 * Floodgate). Exactly one of the two is non-null.</p>
 *
 * @param predicate       condition to check after state-based actions, or {@code null}
 * @param sourcePredicate layer-aware condition on the source permanent, or {@code null}
 * @param referencedPermanentPredicate selects a non-targeting permanent reference when the
 *                                     trigger fires, or {@code null}
 * @param effects         effects to put on the stack when triggered
 * @param description     log/stack description for the triggered ability
 *
 * <p>The optional {@code referencedPermanentPredicate} is evaluated across all battlefields and
 * stores the first matching permanent as a non-targeting reference on the triggered stack entry.</p>
 */
public record StateTriggerEffect(
        StateTriggerPredicate predicate,
        PermanentPredicate sourcePredicate,
        PermanentPredicate referencedPermanentPredicate,
        List<CardEffect> effects,
        String description
) implements CardEffect {

    public StateTriggerEffect(StateTriggerPredicate predicate, List<CardEffect> effects, String description) {
        this(predicate, null, null, effects, description);
    }

    public StateTriggerEffect(StateTriggerPredicate predicate,
                              PermanentPredicate referencedPermanentPredicate,
                              List<CardEffect> effects, String description) {
        this(predicate, null, referencedPermanentPredicate, effects, description);
    }

    public StateTriggerEffect(PermanentPredicate sourcePredicate, List<CardEffect> effects, String description) {
        this(null, sourcePredicate, null, effects, description);
    }
}
