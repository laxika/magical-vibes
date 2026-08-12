package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardPerChosenTypeCountEffect;

@CardRegistration(set = "ECL", collectorNumber = "54")
@CardRegistration(set = "ECL", collectorNumber = "357")
@CardRegistration(set = "ECL", collectorNumber = "384")
@CardRegistration(set = "ECL", collectorNumber = "394")
@CardRegistration(set = "ECL", collectorNumber = "408")
public class HarmonizedCrescendo extends Card {

    public HarmonizedCrescendo() {
        addEffect(EffectSlot.SPELL, new DrawCardPerChosenTypeCountEffect());
    }
}
