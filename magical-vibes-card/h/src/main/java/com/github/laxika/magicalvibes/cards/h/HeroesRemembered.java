package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "7")
public class HeroesRemembered extends Card {

    public HeroesRemembered() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(20));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(),
                "Suspend 10\u2014{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(10));
    }
}
