package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * UPKEEP_TRIGGERED: for each player (APNAP), this permanent deals {@code damage} to that player
 * unless they pay {@code manaCost}. Each player chooses independently — paying avoids only their
 * own damage; declining (or being unable to pay) deals the damage to that player before the next
 * is offered. Used by Lim-Dûl's Hex ({@code "{B/3}"} for "pay {B} or {3}").
 *
 * <p>{@code manaCost} is a normal mana-cost string (including monocolored hybrid like
 * {@code "{B/3}"}). Routed through the damage system so prevention/redirection/infect apply.
 */
public record EachPlayerTakesDamageUnlessPaysEffect(int damage, String manaCost)
        implements DamageDealingEffect {

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
