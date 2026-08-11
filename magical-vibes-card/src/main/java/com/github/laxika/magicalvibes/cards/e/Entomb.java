package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "ODY", collectorNumber = "132")
public class Entomb extends Card {

    public Entomb() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(null, LibrarySearchDestination.GRAVEYARD));
    }
}
