package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "164")
public class EaglesOfTheNorth extends Card {

    public EaglesOfTheNorth() {
        // When this creature enters, creatures you control get +1/+0 and gain first strike until end of turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                BoostEffect.creaturesYouControl(1, 0),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.creaturesYouControl()));
    }
}
