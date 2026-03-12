package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect for leyline cards: if in opening hand, the player may begin the game
 * with this card on the battlefield (CR 103.6a). Used as the wrapped effect inside a
 * MayEffect queued during the pregame procedure.
 */
public record LeylineStartOnBattlefieldEffect() implements CardEffect {
}
