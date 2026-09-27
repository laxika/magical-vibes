package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "55")
public class Ketria extends Card {

    public Ketria() {
        ChooseOneForTargetCreatureEffect counterChoice = new ChooseOneForTargetCreatureEffect(List.of(
                counterMode("Put a vigilance counter on it", CounterType.VIGILANCE),
                counterMode("Put a menace counter on it", CounterType.MENACE),
                counterMode("Put a trample counter on it", CounterType.TRAMPLE)));
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, counterChoice)
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, counterChoice);

        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect());
    }

    private static ChooseOneEffect.ChooseOneOption counterMode(String label, CounterType counterType) {
        return new ChooseOneEffect.ChooseOneOption(label,
                new PutCounterOnTargetPermanentEffect(counterType));
    }
}
