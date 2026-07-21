package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "95")
public class PutridLeech extends Card {

    public PutridLeech() {
        // Pay 2 life: This creature gets +2/+2 until end of turn. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2), new BoostSelfEffect(2, 2)),
                "Pay 2 life: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1));
    }
}
