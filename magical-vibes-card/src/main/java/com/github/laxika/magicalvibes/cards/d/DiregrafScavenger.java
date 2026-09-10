package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "105")
public class DiregrafScavenger extends Card {

    public DiregrafScavenger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileCardsFromGraveyardEffect(1, new CardTypePredicate(CardType.CREATURE), 2, 2, true));
    }
}
