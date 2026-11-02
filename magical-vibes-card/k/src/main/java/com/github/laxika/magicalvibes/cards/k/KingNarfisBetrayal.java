package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * King Narfi's Betrayal — {1}{U}{B} Enchantment — Saga
 *
 * I — Each player mills four cards. Then you may exile a creature or planeswalker card from each
 * graveyard.
 * II, III — Until end of turn, you may cast spells from among cards exiled with this Saga, and you
 * may spend mana as though it were mana of any color to cast those spells.
 */
@CardRegistration(set = "KHM", collectorNumber = "219")
@CardRegistration(set = "LCC", collectorNumber = "272")
public class KingNarfisBetrayal extends Card {

    public KingNarfisBetrayal() {
        addTrigger(Triggers.chapter(1), new MillEffect(4, MillRecipient.CONTROLLER));
        addTrigger(Triggers.chapter(1), new MillEffect(4, MillRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(1),
                new ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER)
                        ))));

        var nonlandCard = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        addTrigger(Triggers.chapter(2),
                new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(nonlandCard));
        addTrigger(Triggers.chapter(3),
                new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(nonlandCard));
    }
}
