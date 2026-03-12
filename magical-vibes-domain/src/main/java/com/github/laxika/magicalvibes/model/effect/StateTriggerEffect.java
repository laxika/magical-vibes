package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * A state-triggered ability (MTG rule 603.8). When the {@code predicate} condition
 * is met and the ability is not already on the stack, the {@code effects} are put
 * onto the stack as a triggered ability. The trigger won't fire again while it's
 * on the stack; once resolved, countered, or otherwise removed, it can fire again.
 *
 * @param predicate   condition to check after state-based actions
 * @param effects     effects to put on the stack when triggered
 * @param description log/stack description for the triggered ability
 */
public record StateTriggerEffect(
        StateTriggerPredicate predicate,
        List<CardEffect> effects,
        String description
) implements CardEffect {
}
