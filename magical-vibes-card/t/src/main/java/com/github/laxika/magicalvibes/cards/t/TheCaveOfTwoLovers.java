package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "126")
public class TheCaveOfTwoLovers extends Card {

    public TheCaveOfTwoLovers() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                2, "Ally", 1, 1, CardColor.WHITE, List.of(CardSubtype.ALLY), Set.of(), Set.of()));

        addTrigger(Triggers.chapter(2), new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        new CardSubtypePredicate(CardSubtype.CAVE))),
                LibrarySearchDestination.HAND));

        addTrigger(Triggers.chapter(3), new EarthbendTargetLandEffect(3));
    }
}
