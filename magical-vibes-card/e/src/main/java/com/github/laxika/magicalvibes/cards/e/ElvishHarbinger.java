package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "LRW", collectorNumber = "207")
@CardRegistration(set = "DD1", collectorNumber = "4")
@CardRegistration(set = "EVG", collectorNumber = "4")
@CardRegistration(set = "LTC", collectorNumber = "527")
public class ElvishHarbinger extends Card {

    public ElvishHarbinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ELF),
                        LibrarySearchDestination.TOP_OF_LIBRARY),
                "Search your library for an Elf card, reveal it, then shuffle and put that card on top?"
        ));

        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
