package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.effect.l.LivingDeathEffect;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "115")
public class LivingEnd extends Card {

    public LivingEnd() {
        addEffect(EffectSlot.SPELL, new LivingDeathEffect());
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(),
                "Suspend 3\u2014{2}{B}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
