package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may play an additional land on each of your turns."
 * Static ability on the permanent — while on the battlefield it raises only its controller's
 * per-turn land-play allowance by one (counted in {@code GameData.getMaxLandsThisTurn}), unlike
 * the symmetric {@link EachPlayerPlaysAdditionalLandEffect}. Registered in
 * {@code EffectSlot.STATIC}. Used by The Gitrog Monster.
 */
public record PlaysAdditionalLandEachTurnEffect() implements CardEffect {
}
