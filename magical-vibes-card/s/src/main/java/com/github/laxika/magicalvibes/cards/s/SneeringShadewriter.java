package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "101")
public class SneeringShadewriter extends Card {

    public SneeringShadewriter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(2));
    }
}
