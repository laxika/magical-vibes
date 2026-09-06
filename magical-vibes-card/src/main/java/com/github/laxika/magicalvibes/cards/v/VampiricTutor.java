package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "161")
@CardRegistration(set = "VIS", collectorNumber = "72")
public class VampiricTutor extends Card {

    public VampiricTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
    }
}
