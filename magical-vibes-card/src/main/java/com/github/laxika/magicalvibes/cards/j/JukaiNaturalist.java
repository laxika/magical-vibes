package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "NEO", collectorNumber = "225")
public class JukaiNaturalist extends Card {

    public JukaiNaturalist() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), 1, CostModificationScope.SELF));
    }
}
