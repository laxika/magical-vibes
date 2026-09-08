package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "EMN", collectorNumber = "76")
public class TakeInventory extends Card {

    public TakeInventory() {
        // Draw a card, then draw cards equal to the number of cards named Take Inventory
        // in your graveyard.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new CardsInGraveyard(
                new CardNamedPredicate("Take Inventory"), CountScope.CONTROLLER)));
    }
}
