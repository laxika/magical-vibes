package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ParallelThoughtsDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileFaceDownPileEffect;

@CardRegistration(set = "SCG", collectorNumber = "44")
public class ParallelThoughts extends Card {

    public ParallelThoughts() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryForCardsToExileFaceDownPileEffect(7));
        addEffect(EffectSlot.STATIC, new ParallelThoughtsDrawReplacementEffect());
    }
}
