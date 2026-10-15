package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "EXO", collectorNumber = "66")
public class MindMaggots extends Card {

    public MindMaggots() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardCardsAndPutCountersOnSourceEffect(
                        new CardTypePredicate(CardType.CREATURE), 2, "creature cards"));
    }
}
