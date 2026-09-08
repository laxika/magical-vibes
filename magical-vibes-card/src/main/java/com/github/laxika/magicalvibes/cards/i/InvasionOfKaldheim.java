package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PyreOfTheWorldTree;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileHandThenDrawAndMayPlayUntilNextTurnEffect;

@CardRegistration(set = "MOM", collectorNumber = "145")
public class InvasionOfKaldheim extends Card {

    public InvasionOfKaldheim() {
        setBackFaceCard(new PyreOfTheWorldTree());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileHandThenDrawAndMayPlayUntilNextTurnEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "PyreOfTheWorldTree";
    }
}
