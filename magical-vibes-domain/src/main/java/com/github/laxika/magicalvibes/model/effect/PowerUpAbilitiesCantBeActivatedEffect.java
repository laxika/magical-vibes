package com.github.laxika.magicalvibes.model.effect;

/**
 * "During that turn, power-up abilities can't be activated" (Kang the Conqueror's extra turn): a floating effect on
 * every player added as that extra turn begins, lasting until end of turn (CR 611.2a), which
 * {@code GameData.powerUpAbilitiesCantBeActivatedThisTurn()} reports.
 */
public record PowerUpAbilitiesCantBeActivatedEffect() implements CardEffect {
}
