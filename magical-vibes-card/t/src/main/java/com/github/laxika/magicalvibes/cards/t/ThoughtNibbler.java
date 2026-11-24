package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ODY", collectorNumber = "107")
public class ThoughtNibbler extends Card {

    public ThoughtNibbler() {
        addStaticEffect(SetControllerMaximumHandSizeEffect.reduce(Subjects.you(), 2));
    }
}
