package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.List;

/**
 * Target opponent reveals their hand; the caster chooses one creature card from it whose colors
 * include any of {@code colors} (an empty list means any color). The caster gains life equal to that
 * creature card's toughness, then the target player discards the chosen card. Talara's Bane
 * (green or white creature).
 */
public record RevealHandChooseCreatureGainLifeDiscardEffect(List<CardColor> colors) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
