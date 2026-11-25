package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "KTK", collectorNumber = "98")
public class ArrowStorm extends Card {

    public ArrowStorm() {
        addSpellEffect(new ConditionalReplacementEffect(
                new Raid(),
                DealDamageEffect.toAnyTarget(4),
                DealDamageEffect.toAnyTarget(new Fixed(5)).unpreventableWhen(new Raid())
        ));
    }
}
