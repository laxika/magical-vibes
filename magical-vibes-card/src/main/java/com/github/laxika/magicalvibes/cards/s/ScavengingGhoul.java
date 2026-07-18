package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "159")
public class ScavengingGhoul extends Card {

    public ScavengingGhoul() {
        // At the beginning of each end step, put a corpse counter on this creature
        // for each creature that died this turn (any player).
        addEffect(EffectSlot.END_STEP_TRIGGERED, new PutCountersOnSelfEffect(
                CounterType.CORPSE, new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));

        // Remove a corpse counter from this creature: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CORPSE),
                        new RegenerateEffect()
                ),
                "Remove a corpse counter from this creature: Regenerate this creature."
        ));
    }
}
