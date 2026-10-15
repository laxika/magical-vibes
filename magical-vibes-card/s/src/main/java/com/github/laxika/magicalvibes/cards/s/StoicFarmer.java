package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "KHC", collectorNumber = "5")
public class StoicFarmer extends Card {

    public StoicFarmer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(
                        ConditionalEffect.unless(
                                new OpponentControlsMoreLands(),
                                new SearchLibraryEffect(plainsFilter(), LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                        ConditionalEffect.unless(
                                new NotCondition(new OpponentControlsMoreLands()),
                                new SearchLibraryEffect(plainsFilter(), LibrarySearchDestination.HAND))));
    }

    private static CardAllOfPredicate plainsFilter() {
        return new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardSubtypePredicate(CardSubtype.PLAINS)));
    }
}
