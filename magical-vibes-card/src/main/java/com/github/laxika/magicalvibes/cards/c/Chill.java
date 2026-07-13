package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "6ED", collectorNumber = "60")
public class Chill extends Card {

    public Chill() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardColorPredicate(CardColor.RED), 2));
    }
}
