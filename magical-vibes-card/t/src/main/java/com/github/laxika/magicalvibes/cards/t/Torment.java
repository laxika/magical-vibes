package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STH", collectorNumber = "73")
public class Torment extends Card {

    public Torment() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-3, 0, Subjects.attachedCreature()));
    }
}
