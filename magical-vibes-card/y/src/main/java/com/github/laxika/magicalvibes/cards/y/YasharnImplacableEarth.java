package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPayLifeOrSacrificeNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "240")
public class YasharnImplacableEarth extends Card {

    public YasharnImplacableEarth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.FOREST),
                        CardPredicateUtils.basicLand())),
                LibrarySearchDestination.HAND));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        CardPredicateUtils.basicLand())),
                LibrarySearchDestination.HAND));
        addEffect(EffectSlot.STATIC, new PlayersCantPayLifeOrSacrificeNonlandPermanentsEffect());
    }
}
