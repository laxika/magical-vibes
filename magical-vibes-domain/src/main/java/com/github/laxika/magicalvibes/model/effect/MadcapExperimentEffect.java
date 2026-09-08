package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Reveals the controller's library until an artifact card is found, puts that artifact onto the
 * battlefield, randomizes the other revealed cards onto the bottom of the library, and deals
 * damage to the controller equal to the number of cards revealed.
 */
public record MadcapExperimentEffect() implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new EventValue();
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean damagesController() {
        return true;
    }
}
