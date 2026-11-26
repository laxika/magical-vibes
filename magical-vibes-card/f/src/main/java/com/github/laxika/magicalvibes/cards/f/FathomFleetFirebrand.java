package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "142")
public class FathomFleetFirebrand extends Card {

    public FathomFleetFirebrand() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(BoostEffect.self(1, 0)), "{1}{R}: Fathom Fleet Firebrand gets +1/+0 until end of turn."));
    }
}
