package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "75")
@CardRegistration(set = "RVR", collectorNumber = "127")
public class TasteForMayhem extends Card {

    public TasteForMayhem() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()))
                .addStaticEffect(new ConditionalEffect(
                        new CardsInHandAtMost(0),
                        new StaticBoostEffect(2, 0, Subjects.attachedCreature())));
    }
}
