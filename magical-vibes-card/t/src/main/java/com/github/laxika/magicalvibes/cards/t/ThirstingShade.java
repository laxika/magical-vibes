package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "87")
public class ThirstingShade extends Card {

    public ThirstingShade() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(BoostEffect.self(1, 1)), "{2}{B}: This creature gets +1/+1 until end of turn."));
    }
}
