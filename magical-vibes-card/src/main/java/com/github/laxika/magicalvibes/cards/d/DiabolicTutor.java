package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandEffect;

public class DiabolicTutor extends Card {

    public DiabolicTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardToHandEffect());
    }
}
