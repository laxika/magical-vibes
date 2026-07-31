package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals {@code amount} damage to the controller of the last red instant or sorcery spell that dealt
 * damage to this effect's controller this turn (Suffocation). The player is looked up at resolution
 * from {@code GameData.lastRedSpellDamagerThisTurn}; if no such spell damaged the controller this
 * turn, or that player has left the game, nothing happens. Not a targeted effect.
 *
 * @param amount how much damage to deal
 */
public record DealDamageToLastRedSpellDamagerEffect(int amount) implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(amount);
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
