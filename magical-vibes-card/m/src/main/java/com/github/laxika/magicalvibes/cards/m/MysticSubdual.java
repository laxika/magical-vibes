package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "57")
public class MysticSubdual extends Card {

    public MysticSubdual() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-2, 0, Subjects.attachedCreature()))
                .addStaticEffect(new LosesAllAbilitiesEffect(Subjects.attachedCreature()));
    }
}
