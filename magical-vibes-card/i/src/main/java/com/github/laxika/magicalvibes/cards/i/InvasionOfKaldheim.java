package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PyreOfTheWorldTree;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileHandThenDrawAndMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "145")
public class InvasionOfKaldheim extends Card {

    public InvasionOfKaldheim() {
        setBackFaceCard(new PyreOfTheWorldTree());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileHandThenDrawAndMayPlayUntilNextTurnEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "PyreOfTheWorldTree";
    }
}
