package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "78")
public class ImperialSeal extends Card {

    public ImperialSeal() {
        // Life loss is added first because an async library search pauses resolution and
        // remaining effects in the same stack entry are not resumed afterwards. The life
        // loss is unconditional, so resolving it before the (mandatory) search is equivalent.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY));
    }
}
