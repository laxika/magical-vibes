package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * At resolution, each player who tapped a land for mana this turn
 * ({@code GameData.playersWhoTappedLandForManaThisTurn}) sacrifices a land of their choice
 * (APNAP simultaneous forced-sacrifice, CR 101.4). Then this source deals {@code damage}
 * damage to each player who sacrificed a permanent matching {@code subtype} this way
 * (last-known info). Used by Desolation ({@code PLAINS}, 2).
 */
public record PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffect(
        CardSubtype subtype, int damage) implements DamageDealingEffect {

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

    @Override
    public boolean damagesController() {
        return true;
    }
}
