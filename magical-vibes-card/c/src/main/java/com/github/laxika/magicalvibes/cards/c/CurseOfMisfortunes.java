package com.github.laxika.magicalvibes.cards.c;

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
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "56")
public class CurseOfMisfortunes extends Card {

    public CurseOfMisfortunes() {
        // At the beginning of your upkeep, you may search your library for a Curse card that
        // doesn't have the same name as a Curse attached to enchanted player, put it onto the
        // battlefield attached to that player, then shuffle.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(SequenceEffect.of(
                new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.CURSE),
                                new CardNotPredicate(new CardSharesNameWithPermanentsPredicate(
                                        Subjects.attachedToPlayers(Subjects.enchantedPlayer(),
                                                new PermanentHasSubtypePredicate(CardSubtype.CURSE)))))),
                        LibrarySearchDestination.KEEP_IN_LIBRARY, null, false),
                new PutCardsOntoBattlefieldEffect(Subjects.foundThisWay()).attachedTo(Subjects.enchantedPlayer()),
                new ShuffleLibraryEffect(false)),
                "Search your library for a Curse card?"
        ));
    }
}
