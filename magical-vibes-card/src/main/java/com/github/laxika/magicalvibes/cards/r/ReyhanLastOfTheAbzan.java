package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;

@CardRegistration(set = "C16", collectorNumber = "40")
public class ReyhanLastOfTheAbzan extends Card {

    public ReyhanLastOfTheAbzan() {
        TriggeringPermanentConditionalEffect counterTransfer = new TriggeringPermanentConditionalEffect(
                new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 1),
                new MayEffect(
                        new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put that many +1/+1 counters on target creature?"));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, counterTransfer);
        addEffect(EffectSlot.ON_DEATH, counterTransfer);
        addEffect(EffectSlot.ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE,
                new MayEffect(
                        new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put that many +1/+1 counters on target creature?"));
    }
}
