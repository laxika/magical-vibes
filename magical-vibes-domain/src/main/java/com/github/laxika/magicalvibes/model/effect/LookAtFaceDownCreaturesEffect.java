package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission for the controller to look at face-down creatures they don't control; floated on a player by
 * {@link GrantStaticEffectToPlayerEffect#untilEndOfTurn} for "until end of turn" (Lumbering Laundry).
 */
public record LookAtFaceDownCreaturesEffect() implements CardEffect {
}
