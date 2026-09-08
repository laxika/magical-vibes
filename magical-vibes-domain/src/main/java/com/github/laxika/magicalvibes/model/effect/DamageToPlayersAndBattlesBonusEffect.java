package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a global static effect that adds damage to damage events whose recipient is a
 * player or battle.
 */
public interface DamageToPlayersAndBattlesBonusEffect extends CardEffect {

    int amount();
}
