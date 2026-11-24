package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOK", collectorNumber = "45")
public class MinamoScrollkeeper extends Card {

    public MinamoScrollkeeper() {
        addStaticEffect(SetControllerMaximumHandSizeEffect.increase(Subjects.you(), 1));
    }
}
