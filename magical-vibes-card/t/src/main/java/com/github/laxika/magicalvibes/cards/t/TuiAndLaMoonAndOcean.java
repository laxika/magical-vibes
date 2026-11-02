package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "96")
@CardRegistration(set = "TLE", collectorNumber = "181")
public class TuiAndLaMoonAndOcean extends Card {

    public TuiAndLaMoonAndOcean() {
        addTrigger(Triggers.permanentYouControlBecomesTapped(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(), new DrawCardEffect(1)));
        addTrigger(Triggers.becomesUntapped(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
