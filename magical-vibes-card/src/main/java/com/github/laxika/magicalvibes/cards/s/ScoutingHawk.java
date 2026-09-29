package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "102")
public class ScoutingHawk extends Card {

    public ScoutingHawk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(
                        new OpponentControlsMoreLands(),
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardSubtypePredicate(CardSubtype.PLAINS))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)));
    }
}
