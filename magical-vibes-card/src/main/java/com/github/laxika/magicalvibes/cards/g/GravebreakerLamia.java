package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ReduceGraveyardSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "THB", collectorNumber = "98")
public class GravebreakerLamia extends Card {

    public GravebreakerLamia() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(null, LibrarySearchDestination.GRAVEYARD));
        addEffect(EffectSlot.STATIC, new ReduceGraveyardSpellCastCostEffect(1));
    }
}
