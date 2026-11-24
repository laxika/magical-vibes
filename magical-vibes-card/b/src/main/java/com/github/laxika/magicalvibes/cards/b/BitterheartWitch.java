package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "88")
public class BitterheartWitch extends Card {

    public BitterheartWitch() {
        // A Curse that can't legally enchant the target player stays in the library (its ruling, CR 303.4g).
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(SequenceEffect.of(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.CURSE),
                        LibrarySearchDestination.KEEP_IN_LIBRARY, null, false),
                new PutCardsOntoBattlefieldEffect(Subjects.foundThisWay())
                        .attachedTo(Subjects.targetPlayerDeclaredElsewhere()),
                new ShuffleLibraryEffect(false)),
                "Search your library for a Curse card?"
        ));
    }
}
