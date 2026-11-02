package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "17")
public class WildgroveSummoner extends Card {

    public WildgroveSummoner() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect(
                        new CardSubtypePredicate(CardSubtype.FOREST),
                        3,
                        4,
                        CardSubtype.TREEFOLK,
                        Set.of(Keyword.REACH, Keyword.HASTE)));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new SeekLibraryEffect(2,
                        new CardSubtypePredicate(CardSubtype.FOREST),
                        LibrarySearchDestination.BATTLEFIELD));
    }
}
