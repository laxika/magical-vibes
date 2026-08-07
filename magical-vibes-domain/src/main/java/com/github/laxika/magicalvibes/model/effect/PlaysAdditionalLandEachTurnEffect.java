package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may play {@code amount} additional land(s) on each of your turns."
 * Static ability on the permanent — while on the battlefield it raises only its controller's
 * per-turn land-play allowance by {@code amount} (counted in {@code GameData.getMaxLandsThisTurn}),
 * unlike the symmetric {@link EachPlayerPlaysAdditionalLandEffect}. Registered in
 * {@code EffectSlot.STATIC}. Used by The Gitrog Monster (1) and Azusa, Lost but Seeking (2).
 *
 * @param amount how many extra land plays the controller gains each of their turns
 */
public record PlaysAdditionalLandEachTurnEffect(int amount) implements CardEffect {
}
