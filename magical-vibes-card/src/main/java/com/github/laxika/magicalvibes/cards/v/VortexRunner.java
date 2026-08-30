package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "STX", collectorNumber = "60")
public class VortexRunner extends Card {

    public VortexRunner() {
        // As long as you control eight or more lands, this creature gets +1/+0 and can't be blocked.
        ControlsPermanentCount eightLands = new ControlsPermanentCount(8, new PermanentIsLandPredicate());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(eightLands,
                new StaticBoostEffect(1, 0, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(eightLands,
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
    }
}
