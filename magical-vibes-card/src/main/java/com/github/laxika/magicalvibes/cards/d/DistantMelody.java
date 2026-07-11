package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardPerChosenTypeCountEffect;

@CardRegistration(set = "MOR", collectorNumber = "32")
public class DistantMelody extends Card {

    public DistantMelody() {
        addEffect(EffectSlot.SPELL, new DrawCardPerChosenTypeCountEffect());
    }
}
