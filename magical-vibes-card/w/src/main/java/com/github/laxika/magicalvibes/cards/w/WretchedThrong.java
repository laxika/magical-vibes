package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "94")
@CardRegistration(set = "VOW", collectorNumber = "91")
@CardRegistration(set = "DBL", collectorNumber = "358")
public class WretchedThrong extends Card {

    public WretchedThrong() {
        // When this creature dies, you may search your library for a card named Wretched Throng,
        // reveal it, put it into your hand, then shuffle.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("Wretched Throng")),
                "Search your library for a card named Wretched Throng?"
        ));
    }
}
