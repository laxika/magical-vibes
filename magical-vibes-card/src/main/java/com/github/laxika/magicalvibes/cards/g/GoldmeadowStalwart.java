package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;

@CardRegistration(set = "LRW", collectorNumber = "18")
public class GoldmeadowStalwart extends Card {

    public GoldmeadowStalwart() {
        // As an additional cost to cast this spell, reveal a Kithkin card from your hand or pay {3}.
        addEffect(EffectSlot.STATIC, new IncreaseOwnCastCostUnlessRevealSubtypeEffect(3, CardSubtype.KITHKIN));
    }
}
