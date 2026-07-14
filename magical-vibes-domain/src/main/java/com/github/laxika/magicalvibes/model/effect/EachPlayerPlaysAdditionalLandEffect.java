package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player may play an additional land during each of their turns."
 * Static ability on the permanent — while on the battlefield it raises every player's
 * per-turn land-play allowance by one (counted in {@code GameData.getMaxLandsThisTurn}).
 * Registered in {@code EffectSlot.STATIC}. Used by Storm Cauldron.
 */
public record EachPlayerPlaysAdditionalLandEffect() implements CardEffect {
}
