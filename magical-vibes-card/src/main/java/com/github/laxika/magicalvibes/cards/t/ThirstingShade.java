package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "87")
public class ThirstingShade extends Card {

    public ThirstingShade() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(new BoostSelfEffect(1, 1)), "{2}{B}: This creature gets +1/+1 until end of turn."));
    }
}
