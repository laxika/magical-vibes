package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "MH1", collectorNumber = "191")
@CardRegistration(set = "STA", collectorNumber = "58")
public class WeatherTheStorm extends Card {

    public WeatherTheStorm() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
