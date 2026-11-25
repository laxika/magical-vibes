package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "PLS", collectorNumber = "66")
public class MagmaBurst extends Card {

    public MagmaBurst() {
        addStaticEffect(new KickerEffect(2, new PermanentIsLandPredicate(), "two lands"));
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        targetWhenKicked(null, 0, 0, 1, 1)
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        DealDamageEffect.toAnyTargetInGroup(3, 0)));
    }
}
