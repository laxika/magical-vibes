package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "38")
@CardRegistration(set = "MSC", collectorNumber = "342")
public class ValeriaRichardsPrecocious extends Card {

    public ValeriaRichardsPrecocious() {
        CardNotPredicate noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(noncreatureSpell, 1, CostModificationScope.SELF));
        addTrigger(Triggers.youCastSpell().withFilter(noncreatureSpell).nthThisTurn(1), new DrawCardEffect());
    }
}
