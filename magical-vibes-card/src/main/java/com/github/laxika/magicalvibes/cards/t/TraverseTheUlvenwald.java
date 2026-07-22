package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "221")
public class TraverseTheUlvenwald extends Card {

    public TraverseTheUlvenwald() {
        // Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        // Delirium — If there are four or more card types among cards in your graveyard, instead
        // search your library for a creature or land card, reveal it, put it into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Delirium(),
                new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.LAND)
                )))
        ));
    }
}
