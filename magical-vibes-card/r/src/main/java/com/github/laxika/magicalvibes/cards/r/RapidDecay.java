package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "UDS", collectorNumber = "67")
public class RapidDecay extends Card {

    public RapidDecay() {
        addSpellEffect(new ExileCardsFromGraveyardEffect(3, 0));
        addCycling("{2}");
    }
}
