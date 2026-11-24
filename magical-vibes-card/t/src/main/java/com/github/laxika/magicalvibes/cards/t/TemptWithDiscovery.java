package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerCanSearchTheirLibraryPredicate;

@CardRegistration(set = "C13", collectorNumber = "174")
@CardRegistration(set = "BLC", collectorNumber = "124")
@CardRegistration(set = "C19", collectorNumber = "183")
@CardRegistration(set = "C16", collectorNumber = "170")
@CardRegistration(set = "LTC", collectorNumber = "532")
public class TemptWithDiscovery extends Card {

    private static final PlayerCanSearchTheirLibraryPredicate CAN_SEARCH = new PlayerCanSearchTheirLibraryPredicate();

    public TemptWithDiscovery() {
        addSpellEffect(searchForLand(Subjects.you()));
        addSpellEffect(TemptingOfferEffect.of(
                "Search your library for a land card and put it onto the battlefield? If you do, its controller "
                        + "searches their library for another land card.",
                TemptWithDiscovery::searchForLand)
                .offeredOnlyTo(Subjects.matching(Subjects.eachOpponent(), CAN_SEARCH)));
        addSpellEffect(new ShuffleLibraryEffect(Subjects.allOf(
                Subjects.matching(Subjects.you(), CAN_SEARCH), Subjects.acceptedPlayers())));
    }

    /** "[players] search their library for a land card and put it onto the battlefield", shuffling at the end. */
    private static CardEffect searchForLand(PlayerSubject players) {
        return EachPlayerSearchesLibraryEffect.mustSearch(players, new CardTypePredicate(CardType.LAND), new Fixed(1),
                LibrarySearchDestination.BATTLEFIELD).leavingLibrariesUnshuffled();
    }
}
