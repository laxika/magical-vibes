package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "121")
public class MayaelsAria extends Card {

    public MayaelsAria() {
        // Single atomic upkeep trigger (one stack entry): the three "Then ..." steps resolve in oracle
        // order, each re-checking the board as it resolves. The +1/+1 counters from step one can push a
        // creature over the power-10 / power-20 thresholds for steps two and three, so they must be one
        // ordered SequenceEffect rather than independent triggers. Each step's "if you control a creature
        // with power N or greater" is a resolution-time condition (not an intervening-if gate).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new ConditionalEffect(
                        new ControlsPermanentCount(1, creatureWithPowerAtLeast(5)),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())),
                new ConditionalEffect(
                        new ControlsPermanentCount(1, creatureWithPowerAtLeast(10)),
                        new GainLifeEffect(10)),
                new ConditionalEffect(
                        new ControlsPermanentCount(1, creatureWithPowerAtLeast(20)),
                        new WinGameEffect())));
    }

    private static PermanentAllOfPredicate creatureWithPowerAtLeast(int minPower) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(minPower)));
    }
}
