package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "CMM", collectorNumber = "279")
public class CrashOfRhinoBeetles extends Card {

    public CrashOfRhinoBeetles() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(10, new PermanentIsLandPredicate()),
                new StaticBoostEffect(10, 10, GrantScope.SELF)));
    }
}
