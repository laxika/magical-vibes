package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "188")
public class Ursapine extends Card {

    public Ursapine() {
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(BoostEffect.target(1, 1)),
                "{G}: Target creature gets +1/+1 until end of turn."));
    }
}
