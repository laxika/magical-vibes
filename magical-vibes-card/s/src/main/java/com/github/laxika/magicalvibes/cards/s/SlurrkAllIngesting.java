package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "312")
public class SlurrkAllIngesting extends Card {

    private static final PermanentAllOfPredicate CONTROLLED_CREATURES_WITH_COUNTERS =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
            ));

    private static final PutCounterOnEachControlledPermanentEffect GROW_COUNTERED_CREATURES =
            new PutCounterOnEachControlledPermanentEffect(
                    CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1), CONTROLLED_CREATURES_WITH_COUNTERS);

    private static final TriggeringPermanentConditionalEffect COUNTERED_CREATURE_DIES =
            new TriggeringPermanentConditionalEffect(
                    new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                    GROW_COUNTERED_CREATURES);

    public SlurrkAllIngesting() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, COUNTERED_CREATURE_DIES);
        addEffect(EffectSlot.ON_DEATH, COUNTERED_CREATURE_DIES);
    }
}
