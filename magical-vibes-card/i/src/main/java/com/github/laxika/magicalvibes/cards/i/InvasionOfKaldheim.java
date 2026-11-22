package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PyreOfTheWorldTree;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "145")
public class InvasionOfKaldheim extends Card {

    public InvasionOfKaldheim() {
        setBackFaceCard(new PyreOfTheWorldTree());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        ExileTargetPlayerHandEffect.handsOf(Subjects.you()),
                        new DrawCardEffect(new AmountThisWay(StepKind.EXILE)),
                        AllowPlayCardsFromExileEffect.untilEndOfYourNextTurn(Subjects.cardsThisWay(StepKind.EXILE))));
    }

    @Override
    public String getBackFaceClassName() {
        return "PyreOfTheWorldTree";
    }
}
