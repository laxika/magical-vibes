package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

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
public class KingNarfisBetrayal extends Card {

    public KingNarfisBetrayal() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MillEffect(4, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MillEffect(4, MillRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER)
                        ))));

        var nonlandCard = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(nonlandCard));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(nonlandCard));
    }
}
