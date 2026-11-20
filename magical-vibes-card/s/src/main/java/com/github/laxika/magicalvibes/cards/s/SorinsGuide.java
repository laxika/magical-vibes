package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "292")
public class SorinsGuide extends Card {

    public SorinsGuide() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(new CardNamedPredicate("Sorin, Vampire Lord")),
                "Search your library and/or graveyard for a card named Sorin, Vampire Lord?"
        ));
    }
}
