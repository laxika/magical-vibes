package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "245")
public class Ferocity extends Card {

    public Ferocity() {
        target(TargetFilters.creature())
                // Whenever enchanted creature blocks or becomes blocked, you may put a +1/+1 counter on it.
                .addTrigger(Triggers.blocks(),
                        TriggerOptions.ONE_OR_MORE,
                        new MayEffect(
                                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                "Put a +1/+1 counter on enchanted creature?"))
                .addTrigger(Triggers.becomesBlocked(),
                        TriggerOptions.ONE_OR_MORE,
                        new MayEffect(
                                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                "Put a +1/+1 counter on enchanted creature?"));
    }
}
