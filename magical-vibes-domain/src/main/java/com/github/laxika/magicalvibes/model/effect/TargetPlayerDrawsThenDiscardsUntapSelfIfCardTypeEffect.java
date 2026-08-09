package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes the targeted player draw a card, then discard a card. If the discarded card has the
 * required type, the source permanent untaps.
 */
public record TargetPlayerDrawsThenDiscardsUntapSelfIfCardTypeEffect(CardType untapIfType)
        implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
