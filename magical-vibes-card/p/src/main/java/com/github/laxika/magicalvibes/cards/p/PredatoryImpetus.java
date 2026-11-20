package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GoadEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C20", collectorNumber = "62")
public class PredatoryImpetus extends Card {

    public PredatoryImpetus() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, Subjects.attachedCreature()))
                .addStaticEffect(new MustBeBlockedIfAbleEffect())
                .addStaticEffect(new GoadEquippedCreatureEffect());
    }
}
