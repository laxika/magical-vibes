package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroy target creature. You gain life equal to that creature's toughness.
 * Used for combat triggers like Engulfing Slagwurm's "blocks or becomes blocked" ability.
 * Life gain occurs regardless of whether destruction succeeds (indestructible/regeneration).
 *
 * <p>When {@code lifeGainCondition} is non-null, life is gained only if the destroyed creature
 * matched that predicate (e.g. Death's Caress: "If that creature was a Human, you gain life equal
 * to its toughness." uses a {@code PermanentHasSubtypePredicate(HUMAN)}). When null, life is
 * always gained.
 */
public record DestroyTargetCreatureAndGainLifeEqualToToughnessEffect(
        PermanentPredicate lifeGainCondition) implements CardEffect {

    public DestroyTargetCreatureAndGainLifeEqualToToughnessEffect() {
        this(null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
