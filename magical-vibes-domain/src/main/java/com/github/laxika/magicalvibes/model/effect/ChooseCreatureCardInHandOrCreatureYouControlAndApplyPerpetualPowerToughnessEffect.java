package com.github.laxika.magicalvibes.model.effect;

/** Chooses a creature card in hand or a controlled creature and perpetually boosts it. */
public record ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect(
        int powerBoost, int toughnessBoost) implements CardEffect {
}
