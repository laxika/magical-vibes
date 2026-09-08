package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "WWK", collectorNumber = "57")
public class DeathsShadow extends Card {

    public DeathsShadow() {
        Scaled lifeTotal = new Scaled(new ControllerLifeTotal(), -1);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                lifeTotal, lifeTotal, GrantScope.SELF));
    }
}
