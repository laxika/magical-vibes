package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "356")
public class DeceptiveLandscape extends Card {

    public DeceptiveLandscape() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Sacrifice this land: Search your library for a basic Plains, Swamp, or Forest card,
        // put it onto the battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardSupertypePredicate(CardSupertype.BASIC),
                                        new CardTypePredicate(CardType.LAND),
                                        new CardAnyOfPredicate(List.of(
                                                new CardSubtypePredicate(CardSubtype.PLAINS),
                                                new CardSubtypePredicate(CardSubtype.SWAMP),
                                                new CardSubtypePredicate(CardSubtype.FOREST))))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{T}, Sacrifice this land: Search your library for a basic Plains, Swamp, or Forest card, "
                        + "put it onto the battlefield tapped, then shuffle."
        ));

        // Cycling {W}{B}{G} ({W}{B}{G}, Discard this card: Draw a card.)
        addCycling("{W}{B}{G}");
    }
}
