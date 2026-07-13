package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "7ED", collectorNumber = "295")
public class FerozsBan extends Card {

    public FerozsBan() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTypePredicate(CardType.CREATURE), 2));
    }
}
