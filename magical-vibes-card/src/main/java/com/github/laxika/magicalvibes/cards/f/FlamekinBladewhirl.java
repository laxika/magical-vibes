package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;

@CardRegistration(set = "LRW", collectorNumber = "165")
public class FlamekinBladewhirl extends Card {

    public FlamekinBladewhirl() {
        // As an additional cost to cast this spell, reveal an Elemental card from your hand or pay {3}.
        addEffect(EffectSlot.STATIC, new IncreaseOwnCastCostUnlessRevealSubtypeEffect(3, CardSubtype.ELEMENTAL));
    }
}
