package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "126")
@CardRegistration(set = "BNG", collectorNumber = "86")
public class WeightOfTheUnderworld extends Card {

    public WeightOfTheUnderworld() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(-3, -2, Subjects.attachedCreature()));
    }
}
