package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "57")
public class PoisonbellyOgre extends Card {

    public PoisonbellyOgre() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
