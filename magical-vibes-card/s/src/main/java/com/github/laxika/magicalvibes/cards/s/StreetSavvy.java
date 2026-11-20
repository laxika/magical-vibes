package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBlockCreaturesWithLandwalkEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "97")
public class StreetSavvy extends Card {

    public StreetSavvy() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(0, 2, Subjects.attachedCreature()))
                .addStaticEffect(new CanBlockCreaturesWithLandwalkEffect());
    }
}
