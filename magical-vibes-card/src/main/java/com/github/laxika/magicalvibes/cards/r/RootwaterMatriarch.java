package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "103")
public class RootwaterMatriarch extends Card {

    public RootwaterMatriarch() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainControlOfEnchantedTargetEffect()),
                true,
                "{T}: Gain control of target creature for as long as that creature is enchanted."
        ));
    }
}
