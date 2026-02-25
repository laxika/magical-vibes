package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "163")
public class Grindclock extends Card {

    public Grindclock() {
        // {T}: Put a charge counter on Grindclock.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutChargeCounterOnSelfEffect()),
                "{T}: Put a charge counter on Grindclock."
        ));

        // {T}: Target player mills X cards, where X is the number of charge counters on Grindclock.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillTargetPlayerByChargeCountersEffect()),
                "{T}: Target player mills X cards, where X is the number of charge counters on Grindclock."
        ));
    }
}
