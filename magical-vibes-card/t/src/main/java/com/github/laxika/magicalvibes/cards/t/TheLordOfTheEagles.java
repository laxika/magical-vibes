package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "HOB", collectorNumber = "46")
public class TheLordOfTheEagles extends Card {

    public TheLordOfTheEagles() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new Max(new Fixed(0), new TotalPowerOfControlledCreatures(
                        new PermanentHasKeywordPredicate(Keyword.FLYING)))));
    }
}
