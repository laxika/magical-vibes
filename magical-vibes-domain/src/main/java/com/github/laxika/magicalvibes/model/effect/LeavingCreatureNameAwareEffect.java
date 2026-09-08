package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger effect whose resolution needs the name of the nontoken creature that left the battlefield.
 */
public interface LeavingCreatureNameAwareEffect {

    CardEffect boundToLeavingCreatureName(String creatureName);
}
