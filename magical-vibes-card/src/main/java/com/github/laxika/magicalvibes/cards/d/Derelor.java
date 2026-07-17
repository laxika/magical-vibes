package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "6ED", collectorNumber = "120")
@CardRegistration(set = "5ED", collectorNumber = "155")
public class Derelor extends Card {

    public Derelor() {
        addEffect(EffectSlot.STATIC, new IncreaseOwnCastCostEffect(
                new CardColorPredicate(CardColor.BLACK), 1));
    }
}
