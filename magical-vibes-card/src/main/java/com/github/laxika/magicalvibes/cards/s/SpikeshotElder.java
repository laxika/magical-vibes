package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSourcePowerToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "104")
public class SpikeshotElder extends Card {

    public SpikeshotElder() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(new DealDamageEqualToSourcePowerToAnyTargetEffect()),
                "{1}{R}{R}: Spikeshot Elder deals damage equal to its power to any target."
        ));
    }
}
