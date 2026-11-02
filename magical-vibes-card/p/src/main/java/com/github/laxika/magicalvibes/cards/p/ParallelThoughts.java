package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ParallelThoughtsDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileFaceDownPileEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "44")
public class ParallelThoughts extends Card {

    public ParallelThoughts() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SearchLibraryForCardsToExileFaceDownPileEffect(7));
        addStaticEffect(new ParallelThoughtsDrawReplacementEffect());
    }
}
