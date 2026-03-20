package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "49")
@CardRegistration(set = "DOM", collectorNumber = "52")
public class Divination extends Card {

    public Divination() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
