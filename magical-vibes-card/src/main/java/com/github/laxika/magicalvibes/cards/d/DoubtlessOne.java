package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "27")
public class DoubtlessOne extends Card {

    public DoubtlessOne() {
        PermanentCount clericsOnBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                clericsOnBattlefield, clericsOnBattlefield));
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
