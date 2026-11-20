package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ORI", collectorNumber = "241")
public class ThrowingKnife extends Card {

    public ThrowingKnife() {
        // Equipped creature gets +2/+0.
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));

        // Whenever equipped creature attacks, you may sacrifice this Equipment.
        // If you do, this Equipment deals 2 damage to any target.
        addTrigger(Triggers.attacks(),
                new MayEffect(SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DealDamageToAnyTargetEffect(2))),
                        "You may sacrifice Throwing Knife. If you do, it deals 2 damage to any target."));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
