package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "34")
public class StallForTime extends Card {

    public StallForTime() {
        addStaticEffect(new KickerEffect("{1}{U}"));
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN)))
                .addSpellEffect(new DrawCardEffect());
    }
}
