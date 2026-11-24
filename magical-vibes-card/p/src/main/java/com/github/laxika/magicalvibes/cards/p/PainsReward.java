package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.BidLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOK", collectorNumber = "85")
public class PainsReward extends Card {

    public PainsReward() {
        addSpellEffect(BidLifeEffect.eachPlayerStartingAtAnyNumber(BidLifeEffect.Prize.CARDS));
        addSpellEffect(new LoseLifeEffect(new AmountThisWay(StepKind.BID), Subjects.playersThisWay(StepKind.BID)));
        addSpellEffect(new DrawCardEffect(4, Subjects.playersThisWay(StepKind.BID)));
    }
}
