package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "91")
public class VisceridDeepwalker extends Card {

    public VisceridDeepwalker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new BoostSelfEffect(1, 0)),
                "{U}: This creature gets +1/+0 until end of turn."
        ));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(),
                "Suspend 4\u2014{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
