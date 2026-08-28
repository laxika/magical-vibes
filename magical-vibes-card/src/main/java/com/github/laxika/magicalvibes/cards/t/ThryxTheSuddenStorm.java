package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

@CardRegistration(set = "THB", collectorNumber = "76")
public class ThryxTheSuddenStorm extends Card {

    public ThryxTheSuddenStorm() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardMinManaValuePredicate(5, true), 1, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC, new ControllerSpellsCantBeCounteredEffect(5));
    }
}
