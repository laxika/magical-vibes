package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "148")
public class DiscipleOfDeceit extends Card {

    public DiscipleOfDeceit() {
        var nonlandCard = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        addTrigger(Triggers.becomesUntapped(),
                new MayEffect(DiscardCardThenEffect.continuing(
                        nonlandCard,
                        new SearchLibraryEffect(null, LibrarySearchDestination.HAND,
                                new ManaValueBound(new LastDiscardedCardManaValue(), true, 0)),
                        "a nonland card"),
                        "Discard a nonland card to search your library for a card with the same mana value?"));
    }
}
