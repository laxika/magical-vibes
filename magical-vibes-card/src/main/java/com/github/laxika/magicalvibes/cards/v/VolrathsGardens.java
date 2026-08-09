package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "124")
public class VolrathsGardens extends Card {

    public VolrathsGardens() {
        // {2}, Tap an untapped creature you control: You gain 2 life. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new GainLifeEffect(2)
                ),
                "{2}, Tap an untapped creature you control: You gain 2 life. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
