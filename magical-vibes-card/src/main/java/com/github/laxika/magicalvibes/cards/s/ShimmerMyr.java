package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "129")
public class ShimmerMyr extends Card {

    public ShimmerMyr() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.ARTIFACT)));
    }
}
