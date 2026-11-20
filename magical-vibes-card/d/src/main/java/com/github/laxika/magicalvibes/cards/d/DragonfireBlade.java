package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.TargetPermanentColorCount;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

@CardRegistration(set = "TDM", collectorNumber = "240")
public class DragonfireBlade extends Card {

    public DragonfireBlade() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addStaticEffect(new GrantEffectEffect(
                TargetingRestrictionEffect.hexproofFromMonocolored(), Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{4}", new ReduceActivationCostEffect(
                new TargetPermanentColorCount())));
    }
}
