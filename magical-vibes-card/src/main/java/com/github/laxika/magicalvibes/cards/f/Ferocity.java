package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "245")
public class Ferocity extends Card {

    public Ferocity() {
        target(TargetFilters.creature())
                // Whenever enchanted creature blocks or becomes blocked, you may put a +1/+1 counter on it.
                .addEffect(EffectSlot.ON_BLOCK, new MayEffect(
                        new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on enchanted creature?"))
                .addEffect(EffectSlot.ON_BECOMES_BLOCKED, new MayEffect(
                        new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on enchanted creature?"));
    }
}
