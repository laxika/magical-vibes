package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAbilityControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "103")
public class FormOfTheDinosaur extends Card {

    public FormOfTheDinosaur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SetLifeTotalEffect(15));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(15),
                        new TargetCreatureDealsPowerDamageToAbilityControllerEffect()));
    }
}
