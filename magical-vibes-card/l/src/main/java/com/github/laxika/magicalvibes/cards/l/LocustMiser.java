package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOK", collectorNumber = "80")
public class LocustMiser extends Card {

    public LocustMiser() {
        addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.eachOpponent(), 2));
    }
}
