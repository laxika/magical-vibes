package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OPC2", collectorNumber = "35")
public class Takenuma extends Card {

    public Takenuma() {
        addEffect(EffectSlot.ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD,
                new DrawCardForTriggeringPermanentControllerEffect());
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                ReturnToHandEffect.target());
    }
}
