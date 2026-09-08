package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that makes a triggered ability of a matching permanent trigger one additional
 * time when a creature dying causes it to trigger.
 *
 * @param includeOwnedEmblemTriggers whether the effect also applies to creature-death triggers
 *                                   of emblems controlled by the equipped creature's controller
 */
public record AdditionalCreatureDeathTriggerEffect(
        PermanentPredicate sourcePredicate,
        boolean includeOwnedEmblemTriggers
) implements CardEffect {

    public AdditionalCreatureDeathTriggerEffect(PermanentPredicate sourcePredicate) {
        this(sourcePredicate, false);
    }
}
