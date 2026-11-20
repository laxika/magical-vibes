package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "307")
public class RowansStalwarts extends Card {

    public RowansStalwarts() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(new CardNamedPredicate("Rowan, Fearless Sparkmage")),
                "Search your library and/or graveyard for a card named Rowan, Fearless Sparkmage?"
        ));
    }
}
