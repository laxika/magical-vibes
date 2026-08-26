package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "FIN", collectorNumber = "72")
public class ScorpionSentinel extends Card {

    public ScorpionSentinel() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(7, new PermanentIsLandPredicate()),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
    }
}
