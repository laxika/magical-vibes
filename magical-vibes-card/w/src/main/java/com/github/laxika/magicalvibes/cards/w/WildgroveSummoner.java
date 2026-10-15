package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "17")
public class WildgroveSummoner extends Card {

    public WildgroveSummoner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect(
                        new CardSubtypePredicate(CardSubtype.FOREST),
                        3,
                        4,
                        CardSubtype.TREEFOLK,
                        Set.of(Keyword.REACH, Keyword.HASTE)));
        addEffect(EffectSlot.ON_DEATH,
                new SeekLibraryEffect(2,
                        new CardSubtypePredicate(CardSubtype.FOREST),
                        LibrarySearchDestination.BATTLEFIELD));
    }
}
