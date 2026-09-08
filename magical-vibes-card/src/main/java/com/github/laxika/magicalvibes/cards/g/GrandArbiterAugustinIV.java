package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "DIS", collectorNumber = "112")
public class GrandArbiterAugustinIV extends Card {

    public GrandArbiterAugustinIV() {
        // White spells you cast cost {1} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardColorPredicate(CardColor.WHITE), 1, CostModificationScope.SELF));

        // Blue spells you cast cost {1} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardColorPredicate(CardColor.BLUE), 1, CostModificationScope.SELF));

        // Spells your opponents cast cost {1} more to cast.
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTruePredicate(), 1, CostModificationScope.OPPONENT));
    }
}
