package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/** Gives the topmost land card in the controller's library a perpetual land-tap mana ability. */
public record PerpetuallyGiveLandExtraManaEffect(ManaColor color) implements CardEffect {
}
