package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "54")
public class RideTheAvalanche extends Card {

    public RideTheAvalanche() {
        addEffect(EffectSlot.SPELL, new GrantFlashToSpellsThisTurnEffect());
        addEffect(EffectSlot.SPELL, new RegisterDelayedControllerSpellCastTriggerEffect(
                null,
                null,
                List.of(PutCounterOnTargetPermanentEffect.upToOneTarget(
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                true,
                false,
                TargetFilters.creature()));
    }
}
