package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class NestingInstinct extends Card {

    public NestingInstinct() {
        addEffect(EffectSlot.SPELL,
                new SeekCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), false));
    }
}
