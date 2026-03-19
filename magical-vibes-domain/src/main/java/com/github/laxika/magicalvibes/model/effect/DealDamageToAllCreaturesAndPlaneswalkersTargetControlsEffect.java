package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each creature and planeswalker the targeted player controls.
 * Non-targeting — piggybacks on a companion targeting effect (e.g. DealDamageToTargetPlayerEffect)
 * that provides the targetId (player) on the same stack entry.
 * Used by Chandra, Bold Pyromancer's -7 ability.
 */
public record DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect(int damage) implements CardEffect {

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
