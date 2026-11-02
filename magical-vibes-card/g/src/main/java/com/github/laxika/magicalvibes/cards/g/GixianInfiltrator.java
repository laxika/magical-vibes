package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "98")
public class GixianInfiltrator extends Card {

    public GixianInfiltrator() {
        addTrigger(Triggers.sacrificed(new PermanentTruePredicate(), ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
