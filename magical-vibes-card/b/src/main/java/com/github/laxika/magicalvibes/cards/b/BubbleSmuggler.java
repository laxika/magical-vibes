package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

@CardRegistration(set = "MKM", collectorNumber = "41")
public class BubbleSmuggler extends Card {

    public BubbleSmuggler() {
        addMorph("{5}{U}");
        addStaticEffect(new PutCountersOnTurnFaceUpEffect(4));
    }
}
