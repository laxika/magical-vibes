package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "AFR", collectorNumber = "79")
public class TrickstersTalisman extends Card {

    public TrickstersTalisman() {
        // Invoke Duplicity — Equipped creature gets +1/+1 and has "Whenever this creature deals combat
        // damage to a player, you may sacrifice Trickster's Talisman. If you do, create a token that's a
        // copy of this creature." The granted ability is controlled by the creature's controller.
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        SequenceEffect.of(new SacrificeSelfEffect(Subjects.granter()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(),
                                        new CreateTokenCopyOfSourceEffect(false, 1))),
                        "You may sacrifice Trickster's Talisman. If you do, create a token that's a copy of this creature.")),
                Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
