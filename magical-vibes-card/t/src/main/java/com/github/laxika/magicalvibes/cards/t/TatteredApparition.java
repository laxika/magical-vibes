package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "111")
public class TatteredApparition extends Card {

    public TatteredApparition() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(BoostEffect.self(1, 1)),
                "{1}{B}: This creature gets +1/+1 until end of turn."));
    }
}
