package com.github.laxika.magicalvibes.model.effect;

/**
 * Global static damage bonus for damage dealt to players and battles, regardless of the source.
 */
public record AdditionalDamageToPlayersAndBattlesEffect(int amount)
        implements DamageToPlayersAndBattlesBonusEffect {
}
