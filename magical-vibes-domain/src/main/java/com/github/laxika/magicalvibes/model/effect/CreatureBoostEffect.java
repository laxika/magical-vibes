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
 * <p>Scope note: this is the targeted-creature boost shape. {@link BoostEffect} implements it for every subject, so
 * readers that mean the targeted shape ask {@link #targetedBoost(CardEffect)}: a self boost, a boost of a named
 * permanent and a non-targeting set boost are not it. Static/anthem boosts (a scope of creatures) are
 * {@link StaticCreatureBoostEffect}.
 */
public interface CreatureBoostEffect extends CardEffect {

    /** The power boost granted, as a {@link DynamicAmount} evaluated at resolution. */
    DynamicAmount powerBoost();

    /** The toughness boost granted, as a {@link DynamicAmount} evaluated at resolution. */
    DynamicAmount toughnessBoost();

    /**
     * {@code effect} as a targeted creature boost, or {@code null}: a {@link BoostEffect} only when its subject
     * declares a target, every other implementer as it is.
     */
    static CreatureBoostEffect targetedBoost(CardEffect effect) {
        if (effect instanceof BoostEffect boost) {
            return boost.subject().declaresTarget() ? boost : null;
        }
        return effect instanceof CreatureBoostEffect boost ? boost : null;
    }
}
