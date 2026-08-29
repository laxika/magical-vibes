package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ONE", collectorNumber = "25")
public class OrthodoxyEnforcer extends Card {

    public OrthodoxyEnforcer() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentIsArtifactPredicate()),
                new StaticBoostEffect(2, 0, GrantScope.SELF)));
    }
}
