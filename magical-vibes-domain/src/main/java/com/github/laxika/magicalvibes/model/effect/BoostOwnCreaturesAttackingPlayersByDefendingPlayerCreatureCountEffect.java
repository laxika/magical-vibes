package com.github.laxika.magicalvibes.model.effect;

/**
 * Boosts each creature controlled by the effect controller that is attacking a player by an
 * amount based on the number of creatures that player controls. Attacks against permanents are
 * excluded because the affected creature must be attacking a player.
 */
public record BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect(
        int powerPerCreature,
        int toughnessPerCreature
) implements CardEffect {

    public BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect() {
        this(1, 0);
    }
}
