package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2175")
public class JawsRelentlessPredator extends Card {

    public JawsRelentlessPredator() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                CreateTokenEffect.ofBloodToken(new EventValue()));
        addTrigger(Triggers.sacrificedOrDestroyed(Triggers.NONCREATURE_ARTIFACT, ObjectRelation.ANY),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }
}
