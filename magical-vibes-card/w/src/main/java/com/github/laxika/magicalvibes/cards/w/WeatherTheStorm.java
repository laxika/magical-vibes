package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "191")
@CardRegistration(set = "STA", collectorNumber = "58")
public class WeatherTheStorm extends Card {

    public WeatherTheStorm() {
        addSpellEffect(new GainLifeEffect(3));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
