package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each creature and planeswalker each opponent controls.
 * Non-targeting — iterates over all opponents' battlefields.
 * Used by cards like Goblin Chainwhirler: "deals 1 damage to each opponent and each creature
 * and planeswalker they control." (paired with DealDamageToEachOpponentEffect for the player damage).
 */
public record DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect(int damage) implements CardEffect {

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
