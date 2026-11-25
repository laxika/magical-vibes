package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

@CardRegistration(set = "AFR", collectorNumber = "250")
public class SpareDagger extends Card {

    public SpareDagger() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.attacks(),
                new MayEffect(
                        SequenceEffect.of(new SacrificeSelfEffect(Subjects.granter()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(),
                                        new QueueReflexiveAbilityEffect(DealDamageEffect.toAnyTarget(1)))),
                        "You may sacrifice Spare Dagger. If you do, this creature deals 1 damage to any target.")),
                Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
