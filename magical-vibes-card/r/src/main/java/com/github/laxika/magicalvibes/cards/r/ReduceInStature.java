package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "72")
public class ReduceInStature extends Card {

    public ReduceInStature() {
        target(TargetFilters.creature())
                .addStaticEffect(new SetBasePowerToughnessEffect(0, 2, Subjects.attachedCreature()));
    }
}
