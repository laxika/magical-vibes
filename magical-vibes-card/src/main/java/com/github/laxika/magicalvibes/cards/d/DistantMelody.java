package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardPerChosenTypeCountEffect;

@CardRegistration(set = "MOR", collectorNumber = "32")
@CardRegistration(set = "H09", collectorNumber = "27")
@CardRegistration(set = "HA1", collectorNumber = "5")
@CardRegistration(set = "ECC", collectorNumber = "45")
public class DistantMelody extends Card {

    public DistantMelody() {
        addEffect(EffectSlot.SPELL, new DrawCardPerChosenTypeCountEffect());
    }
}
