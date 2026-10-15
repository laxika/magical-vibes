package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

/**
 * Pyrrhic Revival — {3}{W/B}{W/B}{W/B} Sorcery
 *
 * Each player returns each creature card from their graveyard to the battlefield with an
 * additional -1/-1 counter on it.
 */
@CardRegistration(set = "EVE", collectorNumber = "93")
public class PyrrhicRevival extends Card {

    public PyrrhicRevival() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        Integer.MAX_VALUE,
                        new CardTypePredicate(CardType.CREATURE),
                        CounterType.MINUS_ONE_MINUS_ONE));
    }
}
