package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class SilvanRally extends Card {

    public SilvanRally() {
        addEffect(EffectSlot.SPELL, new MillControllerAndMayReturnMilledPermanentToHandEffect(
                4, new CardTypePredicate(CardType.LAND), 2));
    }
}
