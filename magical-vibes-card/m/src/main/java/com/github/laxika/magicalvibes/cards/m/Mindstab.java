package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "119")
@CardRegistration(set = "TSR", collectorNumber = "123")
public class Mindstab extends Card {

    public Mindstab() {
        addSpellEffect(new DiscardEffect(3, Subjects.targetPlayer()));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{B}",
                List.of(),
                "Suspend 4—{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
