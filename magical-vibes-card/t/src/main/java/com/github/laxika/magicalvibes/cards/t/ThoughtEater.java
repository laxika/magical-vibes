package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ODY", collectorNumber = "106")
public class ThoughtEater extends Card {

    public ThoughtEater() {
        addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.you(), 3));
    }
}
