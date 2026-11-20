package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "180")
public class SenselessRage extends Card {

    public SenselessRage() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addCastingOption(new MadnessCast("{1}{R}"));
    }
}
