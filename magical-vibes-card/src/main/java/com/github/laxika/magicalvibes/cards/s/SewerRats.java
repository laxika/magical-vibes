package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "139")
public class SewerRats extends Card {

    public SewerRats() {
        // {B}, Pay 1 life: This creature gets +1/+0 until end of turn. Activate no more than three times each turn.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PayLifeCost(1), new BoostSelfEffect(1, 0)),
                "{B}, Pay 1 life: Sewer Rats gets +1/+0 until end of turn. Activate no more than three times each turn.",
                3));
    }
}
