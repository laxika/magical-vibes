package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes the target permanent from combat.
 * If the target is attacking, it stops attacking.
 * If the target is blocking, it stops blocking.
 */
public record RemoveTargetFromCombatEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
