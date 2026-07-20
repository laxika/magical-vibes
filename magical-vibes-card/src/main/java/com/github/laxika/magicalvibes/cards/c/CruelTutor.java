package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "87")
public class CruelTutor extends Card {

    public CruelTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
    }
}
