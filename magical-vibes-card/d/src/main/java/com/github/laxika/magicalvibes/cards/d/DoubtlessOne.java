package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "27")
public class DoubtlessOne extends Card {

    public DoubtlessOne() {
        PermanentCount clericsOnBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC), CountScope.ANY_PLAYER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                clericsOnBattlefield, clericsOnBattlefield));
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
