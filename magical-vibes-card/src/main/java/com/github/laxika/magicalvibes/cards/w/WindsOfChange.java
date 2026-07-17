package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;

@CardRegistration(set = "POR", collectorNumber = "156")
@CardRegistration(set = "5ED", collectorNumber = "275")
public class WindsOfChange extends Card {

    public WindsOfChange() {
        // Each player shuffles the cards from their hand into their library, then draws that many cards.
        addEffect(EffectSlot.SPELL, new ShuffleHandIntoLibraryAndDrawEffect());
    }
}
