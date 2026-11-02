package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "53")
@CardRegistration(set = "DBL", collectorNumber = "53")
public class FirmamentSage extends Card {

    public FirmamentSage() {
        addStaticEffect(new BecomeDayAsEntersEffect());
        addTrigger(Triggers.dayBecomesNightOrNightBecomesDay(), new DrawCardEffect());
    }
}
