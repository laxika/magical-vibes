package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "30")
public class Talonrend extends Card {

    public Talonrend() {
        addActivatedAbility(new ActivatedAbility(false, "{U/R}", List.of(BoostEffect.self(1, -1)), "{U/R}: This creature gets +1/-1 until end of turn."));
    }
}
