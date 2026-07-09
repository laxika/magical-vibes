package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "9ED", collectorNumber = "82")
public class ImaginaryPet extends Card {

    public ImaginaryPet() {
        // At the beginning of your upkeep, if you have a card in hand, return this creature to its owner's hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new CardsInHandAtLeast(1), ReturnToHandEffect.self()));
    }
}
