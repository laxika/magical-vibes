package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FDN", collectorNumber = "70")
public class SoulShackledZombie extends Card {

    public SoulShackledZombie() {
        // When this creature enters, exile up to two target cards from a single graveyard. If at
        // least one creature card was exiled this way, each opponent loses 2 life and you gain 2 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileCardsFromGraveyardEffect(2, new CardTypePredicate(CardType.CREATURE), 2, 2, true));
    }
}
