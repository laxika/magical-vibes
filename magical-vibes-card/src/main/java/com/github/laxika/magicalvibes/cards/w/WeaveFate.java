package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KTK", collectorNumber = "61")
public class WeaveFate extends Card {

    public WeaveFate() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
