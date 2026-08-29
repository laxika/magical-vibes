package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandWithManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "306")
public class MercadianLift extends Card {

    public MercadianLift() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.WINCH)),
                "{1}, {T}: Put a winch counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.WINCH),
                        new PutCreatureFromHandWithManaValueXEffect()
                ),
                "{T}, Remove X winch counters from this artifact: You may put a creature card with mana value X "
                        + "from your hand onto the battlefield."
        ));
    }
}
