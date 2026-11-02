package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "12")
@CardRegistration(set = "DBL", collectorNumber = "12")
public class CelestusSanctifier extends Card {

    public CelestusSanctifier() {
        addStaticEffect(new BecomeDayAsEntersEffect());
        addTrigger(Triggers.dayBecomesNightOrNightBecomesDay(),
                LookAtTopCardsEffect.putOneIntoGraveyardRestOnTop(2));
    }
}
