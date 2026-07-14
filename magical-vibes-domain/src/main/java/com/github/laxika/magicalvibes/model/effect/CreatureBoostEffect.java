package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Capability interface for effects that grant a P/T boost to a single <em>targeted</em> creature
 * (the combat-trick / pump-spell shape). Lets consumers — chiefly the AI evaluators/classifiers —
 * ask "how much power and toughness" without knowing the concrete effect type, mirroring how
 * {@link ManaProducingEffect} abstracts mana production.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components, never a score.
 *
 * <p>Scope note: this is the targeted-creature boost shape. Self-boosts (source pumps itself) and
 * static/anthem boosts (a scope of creatures) are different shapes and do not implement this
 * interface.
 */
public interface CreatureBoostEffect extends CardEffect {

    /** The power boost granted, as a {@link DynamicAmount} evaluated at resolution. */
    DynamicAmount powerBoost();

    /** The toughness boost granted, as a {@link DynamicAmount} evaluated at resolution. */
    DynamicAmount toughnessBoost();
}
