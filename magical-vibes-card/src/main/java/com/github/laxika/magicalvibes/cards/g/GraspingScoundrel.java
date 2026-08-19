package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "RIX", collectorNumber = "74")
public class GraspingScoundrel extends Card {

    public GraspingScoundrel() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsAttacking(),
                new BoostSelfEffect(1, 0)));
    }
}
