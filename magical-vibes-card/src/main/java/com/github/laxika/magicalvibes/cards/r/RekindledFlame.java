package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "EVE", collectorNumber = "61")
public class RekindledFlame extends Card {

    public RekindledFlame() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));

        // At the beginning of your upkeep, if an opponent has no cards in hand, you may return
        // this card from your graveyard to your hand. The intervening-"if" is checked at trigger
        // time in StepTriggerService; the MayEffect prompt is offered only when the gate is met.
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new ConditionalEffect(new AnOpponentHandEmpty(),
                        new MayEffect(ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                                "Return Rekindled Flame from your graveyard to your hand?")));
    }
}
