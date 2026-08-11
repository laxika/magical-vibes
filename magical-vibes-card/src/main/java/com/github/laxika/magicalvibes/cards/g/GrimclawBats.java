package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "45")
public class GrimclawBats extends Card {

    public GrimclawBats() {
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PayLifeCost(1), new BoostSelfEffect(1, 1)),
                "{B}, Pay 1 life: This creature gets +1/+1 until end of turn."));
    }
}
