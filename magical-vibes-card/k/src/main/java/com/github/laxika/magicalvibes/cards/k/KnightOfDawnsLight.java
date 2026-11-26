package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalLifeGainEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "23")
public class KnightOfDawnsLight extends Card {

    public KnightOfDawnsLight() {
        addStaticEffect(new AdditionalLifeGainEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(BoostEffect.self(1, 1)),
                "{1}{W}: This creature gets +1/+1 until end of turn."
        ));
    }
}
