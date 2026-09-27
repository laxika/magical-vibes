package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PhaseOutAllCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "MH2", collectorNumber = "23")
public class OutOfTime extends Card {

    public OutOfTime() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PhaseOutAllCreaturesUntilSourceLeavesEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
    }
}
