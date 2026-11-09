package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "115")
@CardRegistration(set = "UMA", collectorNumber = "176")
public class PatternOfRebirth extends Card {

    public PatternOfRebirth() {
        target(TargetFilters.creature())
                // When enchanted creature dies, that creature's controller may search their library for a creature
                // card, put that card onto the battlefield, then shuffle.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new MayEffect(new SearchLibraryEffect(new Fixed(1), new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER),
                                "Search your library for a creature card and put it onto the battlefield?",
                                null, MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
