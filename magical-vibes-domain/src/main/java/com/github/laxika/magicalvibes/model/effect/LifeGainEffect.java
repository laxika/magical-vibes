package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Capability interface for effects that make a player gain a single evaluated amount of life. Lets
 * consumers — chiefly the AI evaluators/classifiers — ask "how much life does this gain" without
 * knowing the concrete effect type, mirroring how {@link ManaProducingEffect} abstracts mana
 * production.
 *
 * <p>Descriptive only: it states a fact drawn from the record's existing components, never a score.
 */
public interface LifeGainEffect extends CardEffect {

    /**
     * The amount of life gained, as a {@link DynamicAmount} evaluated at resolution (fixed number,
     * X paid, "for each …", …).
     */
    DynamicAmount lifeGainAmount();
}
