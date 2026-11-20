package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "LRW", collectorNumber = "260")
@CardRegistration(set = "MMA", collectorNumber = "214")
public class RunedStalactite extends Card {

    public RunedStalactite() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        // "is every creature type" (without granting the changeling ability)
        addStaticEffect(new GrantAllCreatureTypesToOwnCreaturesEffect(Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
