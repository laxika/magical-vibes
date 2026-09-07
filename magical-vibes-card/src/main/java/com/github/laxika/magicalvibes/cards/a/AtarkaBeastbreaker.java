package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "174")
public class AtarkaBeastbreaker extends Card {

    public AtarkaBeastbreaker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(new BoostSelfEffect(4, 4)),
                "{4}{G}: This creature gets +4/+4 until end of turn. Activate only if creatures you control have total power 8 or greater."
        ).withActivationCondition(
                new ControlledCreaturesTotalPowerAtLeast(8),
                "Activate only if creatures you control have total power 8 or greater."
        ));
    }
}
