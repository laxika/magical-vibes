package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "88")
@CardRegistration(set = "PIP", collectorNumber = "616")
public class WellRested extends Card {

    public WellRested() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ANY_PERMANENT_BECOMES_UNTAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsHostOfSourceAuraPredicate(),
                        new OncePerTurnTriggerEffect(SequenceEffect.of(
                                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                                new GainLifeEffect(2),
                                new DrawCardEffect(1)))));
    }
}
