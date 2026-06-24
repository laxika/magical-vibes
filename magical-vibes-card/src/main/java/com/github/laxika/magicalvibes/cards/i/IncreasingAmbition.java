package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToHandEffect;

@CardRegistration(set = "DKA", collectorNumber = "69")
public class IncreasingAmbition extends Card {

    public IncreasingAmbition() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardsToHandEffect(null, 1, 2));
        addCastingOption(new FlashbackCast("{7}{B}"));
    }
}
