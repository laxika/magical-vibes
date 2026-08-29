package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandCardsToHandEffect;

@CardRegistration(set = "WWK", collectorNumber = "42")
public class TreasureHunt extends Card {

    public TreasureHunt() {
        addEffect(EffectSlot.SPELL, new RevealUntilNonlandCardsToHandEffect());
    }
}
