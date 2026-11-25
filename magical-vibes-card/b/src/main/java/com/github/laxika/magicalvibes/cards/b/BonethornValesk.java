package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "82")
public class BonethornValesk extends Card {

    public BonethornValesk() {
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.ANY),
                DealDamageEffect.toAnyTarget(1));
    }
}
