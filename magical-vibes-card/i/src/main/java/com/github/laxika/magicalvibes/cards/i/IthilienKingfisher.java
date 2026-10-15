package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "HOC", collectorNumber = "177")
public class IthilienKingfisher extends Card {

    public IthilienKingfisher() {
        // When this creature dies, draw a card.
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
