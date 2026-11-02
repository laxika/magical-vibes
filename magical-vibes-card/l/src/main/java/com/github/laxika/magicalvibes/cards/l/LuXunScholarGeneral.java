package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "48")
@CardRegistration(set = "ME3", collectorNumber = "42")
@CardRegistration(set = "C13", collectorNumber = "49")
@CardRegistration(set = "CMA", collectorNumber = "41")
public class LuXunScholarGeneral extends Card {

    public LuXunScholarGeneral() {
        // Horsemanship is a Scryfall keyword (auto-loaded).
        // Whenever Lu Xun deals damage to an opponent, you may draw a card.
        // Not limited to combat damage — Triggers.dealsDamageToPlayer() covers any damage.
        addTrigger(Triggers.dealsDamageToPlayer(), new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
