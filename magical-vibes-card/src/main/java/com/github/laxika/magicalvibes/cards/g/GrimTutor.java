package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "S99", collectorNumber = "79")
public class GrimTutor extends Card {

    public GrimTutor() {
        // Resolve the unconditional life loss before the asynchronous library search.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(3));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
    }
}
