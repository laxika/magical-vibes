package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "240")
public class BanditsHaul extends Card {

    public BanditsHaul() {
        addEffect(EffectSlot.ON_CONTROLLER_COMMITS_CRIME,
                new OncePerTurnTriggerEffect(new PutCountersOnSelfEffect(CounterType.LOOT)));

        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.LOOT),
                        new DrawCardEffect(1)
                ),
                "{2}, {T}, Remove two loot counters from Bandit's Haul: Draw a card."
        ));
    }
}
