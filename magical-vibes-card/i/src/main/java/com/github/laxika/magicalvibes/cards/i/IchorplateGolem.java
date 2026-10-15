package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureHasCountersConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "ONE", collectorNumber = "230")
public class IchorplateGolem extends Card {

    public IchorplateGolem() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureHasCountersConditionalEffect(
                        CounterType.OIL,
                        new PutCountersOnEnteringCreatureEffect(CounterType.OIL, 1, false, CounterType.OIL)));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasCountersPredicate(CounterType.OIL)));
    }
}
