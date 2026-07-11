package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "PTK", collectorNumber = "48")
public class LuXunScholarGeneral extends Card {

    public LuXunScholarGeneral() {
        // Horsemanship is a Scryfall keyword (auto-loaded).
        // Whenever Lu Xun deals damage to an opponent, you may draw a card.
        // Not limited to combat damage — ON_DAMAGE_TO_PLAYER covers any damage.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
