package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "152")
@CardRegistration(set = "DBL", collectorNumber = "152")
public class ObsessiveAstronomer extends Card {

    public ObsessiveAstronomer() {
        addStaticEffect(new BecomeDayAsEntersEffect());
        addTrigger(Triggers.dayBecomesNightOrNightBecomesDay(), new DiscardUpToThenDrawThatManyEffect(2));
    }
}
