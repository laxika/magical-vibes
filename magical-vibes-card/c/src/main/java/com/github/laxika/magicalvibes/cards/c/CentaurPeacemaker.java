package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "158")
public class CentaurPeacemaker extends Card {

    public CentaurPeacemaker() {
        // When this creature enters, each player gains 4 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new GainLifeEffect(4),
                new GainLifeEffect(new Fixed(4), GainLifeRecipient.OPPONENT)));
    }
}
