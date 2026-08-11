package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent exiles cards from the top of their library until the exiled cards have total mana
 * value at least {@code totalManaValueThreshold}. Until end of turn, the effect's controller may
 * cast the exiled nonland cards without paying their mana costs.
 */
public record EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffect(
        int totalManaValueThreshold
) implements CardEffect {
}
