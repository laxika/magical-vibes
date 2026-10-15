package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "MOM", collectorNumber = "150")
public class KarsusDepthguard extends Card {

    public KarsusDepthguard() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourcePowerAtLeast(5),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
