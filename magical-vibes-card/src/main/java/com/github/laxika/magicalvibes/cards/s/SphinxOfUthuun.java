package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "M12", collectorNumber = "76")
@CardRegistration(set = "M13", collectorNumber = "69")
public class SphinxOfUthuun extends Card {

    public SphinxOfUthuun() {
        // When this creature enters, reveal the top five cards of your library. An opponent
        // separates those cards into two piles. Put one pile into your hand and the other
        // into your graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsAndSeparateEffect(5));
    }
}
