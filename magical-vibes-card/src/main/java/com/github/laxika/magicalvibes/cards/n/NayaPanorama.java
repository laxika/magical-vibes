package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "227")
public class NayaPanorama extends Card {

    public NayaPanorama() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        // {1}, {T}, Sacrifice Naya Panorama: Search your library for a basic Mountain, Forest, or
        // Plains card, put it onto the battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardSupertypePredicate(CardSupertype.BASIC),
                                        new CardTypePredicate(CardType.LAND),
                                        new CardAnyOfPredicate(List.of(
                                                new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                                                new CardSubtypePredicate(CardSubtype.FOREST),
                                                new CardSubtypePredicate(CardSubtype.PLAINS))))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{1}, {T}, Sacrifice Naya Panorama: Search your library for a basic Mountain, Forest, or Plains card, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
