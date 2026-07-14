package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "118")
public class DeityOfScars extends Card {

    public DeityOfScars() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSourceEffect(-1, -1, 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/G}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.MINUS_ONE_MINUS_ONE),
                        new RegenerateEffect()
                ),
                "{B/G}, Remove a -1/-1 counter from Deity of Scars: Regenerate Deity of Scars."
        ));
    }
}
