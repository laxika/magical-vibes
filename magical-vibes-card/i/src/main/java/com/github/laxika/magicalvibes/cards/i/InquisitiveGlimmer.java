package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceRoomUnlockCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DSK", collectorNumber = "217")
public class InquisitiveGlimmer extends Card {

    public InquisitiveGlimmer() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), 1, CostModificationScope.SELF));
        addStaticEffect(new ReduceRoomUnlockCostEffect(1));
    }
}
