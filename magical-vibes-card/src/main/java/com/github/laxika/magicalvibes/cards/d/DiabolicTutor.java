package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "135")
@CardRegistration(set = "M10", collectorNumber = "91")
@CardRegistration(set = "M11", collectorNumber = "93")
public class DiabolicTutor extends Card {

    public DiabolicTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardToHandEffect());
    }
}
