package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "64")
public class GoblinRazerunners extends Card {

    public GoblinRazerunners() {
        // {1}{R}, Sacrifice a land: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{1}{R}, Sacrifice a land: Put a +1/+1 counter on this creature."
        ));

        // At the beginning of your end step, you may have this creature deal damage equal to
        // the number of +1/+1 counters on it to target player or planeswalker.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                        "Deal damage equal to the number of +1/+1 counters to target player or planeswalker?"));
    }
}
