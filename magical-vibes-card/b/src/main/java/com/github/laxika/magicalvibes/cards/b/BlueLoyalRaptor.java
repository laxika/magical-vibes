package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "REX", collectorNumber = "8")
@CardRegistration(set = "REX", collectorNumber = "33")
public class BlueLoyalRaptor extends Card {

    public BlueLoyalRaptor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(1),
                        new CardNamedPredicate("Owen Grady, Raptor Trainer"),
                        LibrarySearchDestination.HAND,
                        LibrarySearchPlayer.TARGET_PLAYER),
                "Put Owen Grady, Raptor Trainer into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addStaticEffect(new ControlledPermanentsEnterWithSourceCountersEffect(
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));
    }
}
