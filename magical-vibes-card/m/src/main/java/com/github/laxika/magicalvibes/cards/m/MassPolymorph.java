package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCreatureCardsToBattlefieldAndShuffleRestEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M11", collectorNumber = "64")
public class MassPolymorph extends Card {

    public MassPolymorph() {
        addSpellEffect(new ExileTargetPermanentEffect(Subjects.creaturesYouControl()));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.exiled(),
                new RevealCreatureCardsToBattlefieldAndShuffleRestEffect(new AmountThisWay(StepKind.EXILE))));
    }
}
