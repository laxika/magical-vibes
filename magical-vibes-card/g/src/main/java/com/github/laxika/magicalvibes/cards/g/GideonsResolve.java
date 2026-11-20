package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "272")
public class GideonsResolve extends Card {

    public GideonsResolve() {
        // When this enchantment enters, you may search your library and/or graveyard for a card
        // named Gideon, Martial Paragon, reveal it, and put it into your hand.
        // If you search your library this way, shuffle.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(new CardNamedPredicate("Gideon, Martial Paragon")),
                "Search your library and/or graveyard for a card named Gideon, Martial Paragon?"
        ));
        // Creatures you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl()));
    }
}
