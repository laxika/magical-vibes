package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "STX", collectorNumber = "142")
public class ScurridColony extends Card {

    public ScurridColony() {
        // This creature gets +2/+2 as long as you control eight or more lands.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentIsLandPredicate()),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
