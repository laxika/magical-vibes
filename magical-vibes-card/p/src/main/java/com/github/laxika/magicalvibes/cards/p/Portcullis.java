package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "139")
public class Portcullis extends Card {

    public Portcullis() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                new ExileTriggeringCreatureUntilSourceLeavesEffect(2));
    }
}
