package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "78")
@CardRegistration(set = "ME2", collectorNumber = "96")
public class ImperialSeal extends Card {

    public ImperialSeal() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
    }
}
