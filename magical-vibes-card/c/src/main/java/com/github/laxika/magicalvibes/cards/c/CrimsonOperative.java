package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "MSH", collectorNumber = "126")
@CardRegistration(set = "MSC", collectorNumber = "848")
public class CrimsonOperative extends Card {

    public CrimsonOperative() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
