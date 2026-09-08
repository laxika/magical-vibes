package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "119")
public class Mindstab extends Card {

    public Mindstab() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(),
                "Suspend 4—{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
