package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "272")
public class DomrisNodorog extends Card {

    public DomrisNodorog() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(new CardNamedPredicate("Domri, City Smasher")),
                "Search your library and/or graveyard for a card named Domri, City Smasher?"
        ));
    }
}
