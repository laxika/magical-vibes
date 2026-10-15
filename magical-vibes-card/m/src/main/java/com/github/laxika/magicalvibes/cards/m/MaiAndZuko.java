package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TLE", collectorNumber = "68")
public class MaiAndZuko extends Card {

    public MaiAndZuko() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.ALLY)));
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.ARTIFACT)));
    }
}
