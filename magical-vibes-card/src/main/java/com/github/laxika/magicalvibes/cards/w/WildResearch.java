package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "72")
public class WildResearch extends Card {

    public WildResearch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        search(CardType.ENCHANTMENT),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER, true),
                        new ShuffleLibraryEffect(false)
                ),
                "{1}{W}: Search your library for an enchantment card and reveal that card. Put it into your hand, then discard a card at random. Then shuffle."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        search(CardType.INSTANT),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER, true),
                        new ShuffleLibraryEffect(false)
                ),
                "{1}{U}: Search your library for an instant card and reveal that card. Put it into your hand, then discard a card at random. Then shuffle."
        ));
    }

    private static SearchLibraryEffect search(CardType cardType) {
        return new SearchLibraryEffect(
                new Fixed(1),
                new CardTypePredicate(cardType),
                LibrarySearchDestination.HAND,
                null,
                1,
                false,
                false,
                false,
                false,
                null,
                LibrarySearchPlayer.CONTROLLER,
                false,
                false,
                false);
    }
}
