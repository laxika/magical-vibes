package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "232")
public class ShrewdStoryteller extends Card {

    public ShrewdStoryteller() {
        target(TargetFilters.creature()).addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                new SurvivalTriggerEffect(new ConditionalEffect(
                        new SourceIsTapped(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
