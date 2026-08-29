package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;

@CardRegistration(set = "CSP", collectorNumber = "50")
public class VexingSphinx extends Card {

    public VexingSphinx() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.discardCard());
        addEffect(EffectSlot.ON_DEATH,
                new DrawCardForEachDyingSourceCounterEffect(CounterType.AGE));
    }
}
