package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;

@CardRegistration(set = "AKH", collectorNumber = "67")
public class SacredExcavation extends Card {

    public SacredExcavation() {
        // Return up to two target cards with cycling from your graveyard to your hand.
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardHasCyclingPredicate(), 2));
    }
}
