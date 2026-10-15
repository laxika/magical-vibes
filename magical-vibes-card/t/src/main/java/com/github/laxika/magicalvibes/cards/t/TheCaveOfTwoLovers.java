package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "126")
public class TheCaveOfTwoLovers extends Card {

    public TheCaveOfTwoLovers() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                2, "Ally", 1, 1, CardColor.WHITE, List.of(CardSubtype.ALLY), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        new CardSubtypePredicate(CardSubtype.CAVE))),
                LibrarySearchDestination.HAND));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new EarthbendTargetLandEffect(3));
    }
}
