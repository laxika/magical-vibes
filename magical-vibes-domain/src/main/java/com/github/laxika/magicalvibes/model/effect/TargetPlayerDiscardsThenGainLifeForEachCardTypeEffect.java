package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Target player discards up to {@code amount} cards. The controller then gains
 * {@code lifePerCard} life for each discarded card of {@code cardType}.
 */
public record TargetPlayerDiscardsThenGainLifeForEachCardTypeEffect(
        int amount, CardType cardType, int lifePerCard) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
