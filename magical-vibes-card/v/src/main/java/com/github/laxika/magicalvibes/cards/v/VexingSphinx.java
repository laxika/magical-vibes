package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "50")
@CardRegistration(set = "DMR", collectorNumber = "73")
public class VexingSphinx extends Card {

    public VexingSphinx() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.discardCard());
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DrawCardForEachDyingSourceCounterEffect(CounterType.AGE));
    }
}
