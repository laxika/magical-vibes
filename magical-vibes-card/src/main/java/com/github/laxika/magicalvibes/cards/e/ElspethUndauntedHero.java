package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "270")
public class ElspethUndauntedHero extends Card {

    public ElspethUndauntedHero() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "+2: Put a +1/+1 counter on each of up to two target creatures.",
                null, +2, null, null,
                List.<TargetFilter>of(TargetFilters.creature(), TargetFilters.creature()), 0, 2));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SearchLibraryAndOrGraveyardForCardsEffect(
                        new CardNamedPredicate("Sunlit Hoplite"), 1, LibrarySearchDestination.BATTLEFIELD)),
                "−2: Search your library and/or graveyard for a card named Sunlit Hoplite and put it onto the battlefield. "
                        + "If you search your library this way, shuffle."));

        ColorManaSymbolsAmongControlledPermanents whiteDevotion =
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE);
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new BoostAllOwnCreaturesEffect(whiteDevotion, whiteDevotion),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES)),
                "−8: Until end of turn, creatures you control gain flying and get +X/+X, where X is your devotion to white."));
    }
}
