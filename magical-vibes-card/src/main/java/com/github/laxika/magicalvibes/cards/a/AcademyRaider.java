package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "M14", collectorNumber = "124")
public class AcademyRaider extends Card {

    public AcademyRaider() {
        // Intimidate is a Scryfall-loaded keyword.
        //
        // "Whenever this creature deals combat damage to a player, you may discard a card. If you do,
        // draw a card." — rummage, so the draw only happens if the discard is made.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
