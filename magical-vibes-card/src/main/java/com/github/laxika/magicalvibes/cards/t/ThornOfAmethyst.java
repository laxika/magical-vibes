package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "262")
public class ThornOfAmethyst extends Card {

    public ThornOfAmethyst() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), 1));
    }
}
