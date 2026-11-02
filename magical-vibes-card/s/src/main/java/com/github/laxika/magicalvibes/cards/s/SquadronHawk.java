package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "33")
@CardRegistration(set = "A25", collectorNumber = "34")
@CardRegistration(set = "EMA", collectorNumber = "31")
public class SquadronHawk extends Card {

    public SquadronHawk() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(new Fixed(3), new CardNamedPredicate("Squadron Hawk"),
                        LibrarySearchDestination.HAND),
                "Search your library for up to three cards named Squadron Hawk?"
        ));
    }
}
