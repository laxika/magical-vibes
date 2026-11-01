package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "155")
public class SizzlingChangeling extends Card {

    public SizzlingChangeling() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
