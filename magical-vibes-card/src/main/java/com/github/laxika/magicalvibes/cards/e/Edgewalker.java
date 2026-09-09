package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "SCG", collectorNumber = "137")
public class Edgewalker extends Card {

    public Edgewalker() {
        addEffect(EffectSlot.STATIC, new ReduceColoredCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.CLERIC), new ManaCost("{W}{B}"),
                CostModificationScope.SELF));
    }
}
