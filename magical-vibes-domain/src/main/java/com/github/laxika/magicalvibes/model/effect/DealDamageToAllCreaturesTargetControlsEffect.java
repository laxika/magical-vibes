package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each creature the targeted player controls.
 * Non-targeting — piggybacks on a companion targeting effect (e.g. DealDamageToTargetPlayerEffect)
 * that provides the targetId (player) on the same stack entry.
 * Used by Radiating Lightning.
 */
public record DealDamageToAllCreaturesTargetControlsEffect(int damage) implements CardEffect {

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
