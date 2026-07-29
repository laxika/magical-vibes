package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * This permanent deals {@code damage} to the player carried on the stack entry's {@code targetId}
 * unless they pay {@code costPerCounter} for each counter of {@code counterType} on the source
 * (Energy Vortex). With zero counters the payment is free, so no damage is dealt and no prompt is
 * shown.
 *
 * <p>Pushed by a trigger slot that bakes the affected player into the stack entry — e.g.
 * {@code OPPONENT_UPKEEP_TRIGGERED}, whose {@code targetId} is the active player.
 *
 * @param damage         damage dealt when the player declines or cannot pay
 * @param counterType    counter kind that scales the payment (e.g. {@link CounterType#VORTEX})
 * @param costPerCounter mana cost paid once per counter (e.g. {@code "{1}"})
 */
public record DamageTargetPlayerUnlessPaysPerCounterEffect(
        int damage, CounterType counterType, String costPerCounter) implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
