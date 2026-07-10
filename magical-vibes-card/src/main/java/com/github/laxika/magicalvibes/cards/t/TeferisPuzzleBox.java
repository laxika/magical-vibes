package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutHandOnBottomOfLibraryAndDrawEffect;

@CardRegistration(set = "9ED", collectorNumber = "312")
public class TeferisPuzzleBox extends Card {

    public TeferisPuzzleBox() {
        // At the beginning of each player's draw step, that player puts the cards in their hand on
        // the bottom of their library in any order, then draws that many cards.
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new PutHandOnBottomOfLibraryAndDrawEffect());
    }
}
