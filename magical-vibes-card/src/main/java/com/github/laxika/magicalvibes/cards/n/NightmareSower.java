package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "114")
public class NightmareSower extends Card {

    public NightmareSower() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        new SpellCastTriggerEffect(
                                null,
                                List.of(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)),
                                null,
                                TargetFilters.creature(),
                                null,
                                true,
                                false));
    }
}
