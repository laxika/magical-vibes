package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "215")
@CardRegistration(set = "PIO", collectorNumber = "306")
public class FrostburnWeird extends Card {

    public FrostburnWeird() {
        addActivatedAbility(new ActivatedAbility(false, "{U/R}", List.of(BoostEffect.self(1, -1)), "{U/R}: This creature gets +1/-1 until end of turn."));
    }
}
