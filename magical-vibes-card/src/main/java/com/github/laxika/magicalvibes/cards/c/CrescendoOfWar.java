package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

@CardRegistration(set = "VMA", collectorNumber = "21")
public class CrescendoOfWar extends Card {

    public CrescendoOfWar() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.STRIFE));

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 0, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate(), CounterType.STRIFE, false));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 0, GrantScope.ALL_OWN_CREATURES, new PermanentIsBlockingPredicate(), CounterType.STRIFE, false));
    }
}
