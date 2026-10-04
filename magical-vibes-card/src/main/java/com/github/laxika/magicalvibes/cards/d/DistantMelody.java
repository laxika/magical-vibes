package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardPerChosenTypeCountEffect;

@CardRegistration(set = "MOR", collectorNumber = "32")
@CardRegistration(set = "H09", collectorNumber = "27")
@CardRegistration(set = "SLD", collectorNumber = "1464")
@CardRegistration(set = "SLD", collectorNumber = "2311")
@CardRegistration(set = "SLD", collectorNumber = "2326")
@CardRegistration(set = "HA1", collectorNumber = "5")
@CardRegistration(set = "ECC", collectorNumber = "45")
@CardRegistration(set = "LCC", collectorNumber = "154")
@CardRegistration(set = "MOC", collectorNumber = "220")
@CardRegistration(set = "MIC", collectorNumber = "98")
@CardRegistration(set = "WOC", collectorNumber = "89")
@CardRegistration(set = "VOC", collectorNumber = "103")
@CardRegistration(set = "SCD", collectorNumber = "48")
public class DistantMelody extends Card {

    public DistantMelody() {
        addEffect(EffectSlot.SPELL, new DrawCardPerChosenTypeCountEffect());
    }
}
