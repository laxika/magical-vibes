package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

@CardRegistration(set = "THB", collectorNumber = "76")
@CardRegistration(set = "CMM", collectorNumber = "127")
@CardRegistration(set = "THB", collectorNumber = "309")
public class ThryxTheSuddenStorm extends Card {

    public ThryxTheSuddenStorm() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardMinManaValuePredicate(5, true), 1, CostModificationScope.SELF));
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(5));
    }
}
