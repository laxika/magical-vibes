package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDrawByManaValueEffect;

@CardRegistration(set = "BOK", collectorNumber = "36")
public class HeedTheMists extends Card {

    public HeedTheMists() {
        addEffect(EffectSlot.SPELL, new MillControllerAndDrawByManaValueEffect());
    }
}
