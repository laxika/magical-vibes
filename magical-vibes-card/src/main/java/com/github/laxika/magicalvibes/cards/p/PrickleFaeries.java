package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;

public class PrickleFaeries extends Card {

    public PrickleFaeries() {
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new DealDamageIfFewCardsInHandEffect(2, 2));
    }
}
