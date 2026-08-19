package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that makes a triggered ability of a matching permanent trigger one additional
 * time when a creature dying causes it to trigger.
 */
public record AdditionalCreatureDeathTriggerEffect(PermanentPredicate sourcePredicate) implements CardEffect {
}
