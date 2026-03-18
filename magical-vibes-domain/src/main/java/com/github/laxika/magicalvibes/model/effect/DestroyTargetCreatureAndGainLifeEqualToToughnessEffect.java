package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy target creature. You gain life equal to that creature's toughness.
 * Used for combat triggers like Engulfing Slagwurm's "blocks or becomes blocked" ability.
 * Life gain occurs regardless of whether destruction succeeds (indestructible/regeneration).
 */
public record DestroyTargetCreatureAndGainLifeEqualToToughnessEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
