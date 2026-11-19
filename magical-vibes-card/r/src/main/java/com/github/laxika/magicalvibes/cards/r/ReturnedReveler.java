package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "JOU", collectorNumber = "79")
public class ReturnedReveler extends Card {

    public ReturnedReveler() {
        // When this creature dies, each player mills three cards.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MillEffect(3, Subjects.eachPlayer()));
    }
}
