package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "CSP", collectorNumber = "80")
public class EarthenGoo extends Card {

    public EarthenGoo() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{R/G}"));
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new CountersOnSource(CounterType.AGE),
                new CountersOnSource(CounterType.AGE),
                GrantScope.SELF));
    }
}
