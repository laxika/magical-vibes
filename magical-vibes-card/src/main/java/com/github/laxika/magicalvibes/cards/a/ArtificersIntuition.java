package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "23")
public class ArtificersIntuition extends Card {

    public ArtificersIntuition() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.ARTIFACT), "an artifact"),
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        new CardMaxManaValuePredicate(1))),
                                LibrarySearchDestination.HAND)
                ),
                "{U}, Discard an artifact card: Search your library for an artifact card with mana value 1 or less, reveal it, put it into your hand, then shuffle."
        ));
    }
}
