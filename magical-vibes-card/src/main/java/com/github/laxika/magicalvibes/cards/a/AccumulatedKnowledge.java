package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "NEM", collectorNumber = "26")
public class AccumulatedKnowledge extends Card {

    public AccumulatedKnowledge() {
        // Draw a card, then draw cards equal to the number of cards named Accumulated Knowledge
        // in all graveyards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new CardsInGraveyard(
                new CardNamedPredicate("Accumulated Knowledge"), CountScope.ANY_PLAYER)));
    }
}
