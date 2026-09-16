package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. If it's a land card, put it onto the
 * battlefield tapped. Otherwise, draw it.
 */
public record RevealTopCardLandToTappedBattlefieldElseDrawEffect() implements CardEffect {
}
