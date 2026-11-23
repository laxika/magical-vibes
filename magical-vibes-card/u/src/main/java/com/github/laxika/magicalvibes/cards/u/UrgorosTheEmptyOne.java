package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "109")
public class UrgorosTheEmptyOne extends Card {

    public UrgorosTheEmptyOne() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new DiscardEffect(1, Subjects.targetPlayer(), true),
                new ConditionalEffect(new NotCondition(HappenedThisWay.discarded()), new DrawCardEffect(1))));
    }
}
