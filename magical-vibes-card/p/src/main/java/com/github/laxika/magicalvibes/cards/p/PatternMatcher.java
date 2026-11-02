package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "234")
public class PatternMatcher extends Card {

    public PatternMatcher() {
        // When this creature enters, you may search your library for a card with the same name as
        // another creature you control, reveal it, put it into your hand, then shuffle.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffect(),
                        "Search your library for a card with the same name as another creature you control?"));
    }
}
