package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;

@CardRegistration(set = "SOK", collectorNumber = "84")
public class OneWithNothing extends Card {

    public OneWithNothing() {
        addSpellEffect(new DiscardHandEffect());
    }
}
