package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

@CardRegistration(set = "SHM", collectorNumber = "28")
public class AdviceFromTheFae extends Card {

    public AdviceFromTheFae() {
        // Look at the top five cards of your library. If you control more creatures than each other
        // player, put two of those cards into your hand. Otherwise, put one of them into your hand.
        // Then put the rest on the bottom of your library in any order.
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(5),
                new FixedIfControlMoreCreaturesThanEachOtherPlayer(2, 1),
                null,
                LookDestination.BOTTOM_OF_LIBRARY,
                false));
    }
}
