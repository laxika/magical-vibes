package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "268")
@CardRegistration(set = "DMR", collectorNumber = "176")
public class SetonsDesire extends Card {

    public SetonsDesire() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()))
                .addStaticEffect(new ConditionalEffect(
                        new GraveyardCardThreshold(7, null),
                        new MustBeBlockedByAllCreaturesEffect()));
    }
}
