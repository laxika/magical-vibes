package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "248")
public class PavelMaliki extends Card {

    public PavelMaliki() {
        addActivatedAbility(new ActivatedAbility(false, "{B}{R}", List.of(new BoostSelfEffect(1, 0)),
                "{B}{R}: Pavel Maliki gets +1/+0 until end of turn."));
    }
}
