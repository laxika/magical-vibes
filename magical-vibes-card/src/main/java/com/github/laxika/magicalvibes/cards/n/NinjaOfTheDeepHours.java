package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "BOK", collectorNumber = "44")
public class NinjaOfTheDeepHours extends Card {

    public NinjaOfTheDeepHours() {
        // Ninjutsu {1}{U}
        addNinjutsu("{1}{U}");

        // Whenever this creature deals combat damage to a player, you may draw a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
