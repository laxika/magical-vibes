package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTransformedPredicate;

@CardRegistration(set = "MOM", collectorNumber = "248")
public class MutagenConnoisseur extends Card {

    public MutagenConnoisseur() {
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new PermanentCount(new PermanentIsTransformedPredicate(), CountScope.CONTROLLER),
                new Fixed(0), GrantScope.SELF));
    }
}
