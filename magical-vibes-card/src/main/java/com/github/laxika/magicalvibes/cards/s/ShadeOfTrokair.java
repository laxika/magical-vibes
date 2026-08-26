package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "18")
public class ShadeOfTrokair extends Card {

    public ShadeOfTrokair() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new BoostSelfEffect(1, 1)),
                "{W}: This creature gets +1/+1 until end of turn."
        ));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(),
                "Suspend 3\u2014{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
