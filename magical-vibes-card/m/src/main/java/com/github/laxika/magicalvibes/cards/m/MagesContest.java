package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BidLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

@CardRegistration(set = "INV", collectorNumber = "154")
public class MagesContest extends Card {

    public MagesContest() {
        addSpellEffect(new BidLifeEffect(Subjects.allOf(Subjects.you(), Subjects.controllerOf(Subjects.targetSpell())),
                1, BidLifeEffect.Prize.COUNTER_TARGET_SPELL));
        addSpellEffect(new LoseLifeEffect(new AmountThisWay(StepKind.BID), Subjects.playersThisWay(StepKind.BID)));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.toAPlayer(StepKind.BID, PlayerRelation.SELF),
                new CounterSpellEffect()));
    }
}
