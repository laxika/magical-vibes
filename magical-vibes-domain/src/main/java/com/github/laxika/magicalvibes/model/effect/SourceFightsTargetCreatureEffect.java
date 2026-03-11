package com.github.laxika.magicalvibes.model.effect;

/**
 * Source permanent and target creature deal damage to each other equal to their respective powers.
 * Used by Cyclops Gladiator and similar "fight" triggered abilities.
 */
public record SourceFightsTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
