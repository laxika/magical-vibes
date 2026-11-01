package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "2")
public class AnafenzaUnyieldingLineage extends Card {

    public AnafenzaUnyieldingLineage() {
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new EndureEffect(2));
    }
}
