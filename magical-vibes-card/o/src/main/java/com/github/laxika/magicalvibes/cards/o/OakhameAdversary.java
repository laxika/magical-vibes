package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "167")
public class OakhameAdversary extends Card {

    public OakhameAdversary() {
        addStaticEffect(new ConditionalEffect(
                new OpponentControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.GREEN))),
                new ReduceOwnCastCostEffect(new Fixed(2))));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
    }
}
