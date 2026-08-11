package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "KTK", collectorNumber = "116")
public class MarduHeartPiercer extends Card {

    public MarduHeartPiercer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Raid(), new DealDamageToAnyTargetEffect(2)));
    }
}
