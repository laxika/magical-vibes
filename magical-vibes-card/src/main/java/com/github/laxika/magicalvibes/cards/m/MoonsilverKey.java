package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasManaAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "255")
public class MoonsilverKey extends Card {

    public MoonsilverKey() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.ARTIFACT),
                                                new CardHasManaAbilityPredicate()
                                        )),
                                        CardPredicateUtils.basicLand()
                                )),
                                LibrarySearchDestination.HAND)
                ),
                "{1}, {T}, Sacrifice Moonsilver Key: Search your library for an artifact card with a mana ability or a basic land card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
