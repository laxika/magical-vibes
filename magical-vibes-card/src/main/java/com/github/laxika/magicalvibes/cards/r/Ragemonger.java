package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "BNG", collectorNumber = "153")
public class Ragemonger extends Card {

    public Ragemonger() {
        addEffect(EffectSlot.STATIC, new ReduceColoredCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.MINOTAUR), new ManaCost("{B}{R}"),
                CostModificationScope.SELF));
    }
}
