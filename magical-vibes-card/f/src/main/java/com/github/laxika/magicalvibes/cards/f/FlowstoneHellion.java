package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "84")
@CardRegistration(set = "VMA", collectorNumber = "161")
public class FlowstoneHellion extends Card {

    public FlowstoneHellion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(BoostEffect.self(1, -1)),
                "{0}: This creature gets +1/-1 until end of turn."
        ));
    }
}
