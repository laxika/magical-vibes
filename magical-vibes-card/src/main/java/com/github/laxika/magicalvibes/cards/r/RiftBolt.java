package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "176")
public class RiftBolt extends Card {

    public RiftBolt() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(),
                "Suspend 1—{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(1));
    }
}
