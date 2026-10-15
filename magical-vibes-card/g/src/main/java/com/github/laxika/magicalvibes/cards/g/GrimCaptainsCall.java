package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "XLN", collectorNumber = "108")
public class GrimCaptainsCall extends Card {

    public GrimCaptainsCall() {
        // Each instruction is its own spell effect so the next one waits for any choice the previous one prompts.
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardSubtypePredicate(CardSubtype.PIRATE), new Fixed(1), false));
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardSubtypePredicate(CardSubtype.VAMPIRE), new Fixed(1), false));
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardSubtypePredicate(CardSubtype.DINOSAUR), new Fixed(1), false));
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardSubtypePredicate(CardSubtype.MERFOLK), new Fixed(1), false));
    }
}
