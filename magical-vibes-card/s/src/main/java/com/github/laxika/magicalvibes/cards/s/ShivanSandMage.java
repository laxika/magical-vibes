package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "108")
@CardRegistration(set = "TSR", collectorNumber = "189")
public class ShivanSandMage extends Card {

    public ShivanSandMage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove two time counters from target permanent or suspended card",
                        new AdjustTimeCountersOnTargetEffect(false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two time counters on target permanent with a time counter on it or suspended card",
                        new AdjustTimeCountersOnTargetEffect(true))
        ))));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{R}",
                List.of(),
                "Suspend 4—{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
