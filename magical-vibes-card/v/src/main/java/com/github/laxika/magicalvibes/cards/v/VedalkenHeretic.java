package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "104")
public class VedalkenHeretic extends Card {

    public VedalkenHeretic() {
        // Whenever this creature deals damage to an opponent, you may draw a card.
        // Not limited to combat damage — Triggers.dealsDamageToPlayer() covers any damage.
        addTrigger(Triggers.dealsDamageToPlayer(), new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
