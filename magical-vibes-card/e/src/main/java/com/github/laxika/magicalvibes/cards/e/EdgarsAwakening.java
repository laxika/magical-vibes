package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INR", collectorNumber = "108")
@CardRegistration(set = "VOW", collectorNumber = "110")
@CardRegistration(set = "DBL", collectorNumber = "377")
public class EdgarsAwakening extends Card {

    public EdgarsAwakening() {
        // Return target creature card from your graveyard to the battlefield.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());

        // When you discard this card, you may pay {B}. When you do, return target creature card
        // from your graveyard to your hand.
        // The "when you do" return is a separate reflexive trigger, so it goes on the stack after the
        // payment and can be responded to (CR 603.12).
        addEffect(EffectSlot.ON_SELF_DISCARDED, new MayPayManaEffect("{B}",
                new QueueReflexiveAbilityEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()),
                "Pay {B} to return a creature card from your graveyard to your hand?"));
    }
}
