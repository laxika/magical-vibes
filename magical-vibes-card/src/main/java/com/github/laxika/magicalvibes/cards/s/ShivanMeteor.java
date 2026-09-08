package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "106")
public class ShivanMeteor extends Card {

    public ShivanMeteor() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(13));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(),
                "Suspend 2—{1}{R}{R}",
                ActivationTimingRestriction.SORCERY_SPEED)
                .withSuspendsSourceFromHand(2));
    }
}
