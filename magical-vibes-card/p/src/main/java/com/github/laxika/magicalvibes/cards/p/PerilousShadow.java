package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "74")
public class PerilousShadow extends Card {

    public PerilousShadow() {
        // {1}{B}: This creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new BoostSelfEffect(2, 2)),
                "{1}{B}: This creature gets +2/+2 until end of turn."));
    }
}
