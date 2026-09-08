package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCardsFromHandIntoLibraryThenEffect;

@CardRegistration(set = "ROE", collectorNumber = "86")
public class SeeBeyond extends Card {

    public SeeBeyond() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new ShuffleCardsFromHandIntoLibraryThenEffect(null));
    }
}
