package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "38")
public class TorchDrake extends Card {

    public TorchDrake() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(BoostEffect.self(1, 0)),
                "{1}{R}: This creature gets +1/+0 until end of turn."));
    }
}
