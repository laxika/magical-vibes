package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelayingShieldDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DelayingShieldUpkeepEffect;

@CardRegistration(set = "ODY", collectorNumber = "17")
public class DelayingShield extends Card {

    public DelayingShield() {
        addEffect(EffectSlot.STATIC, new DelayingShieldDamageReplacementEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DelayingShieldUpkeepEffect());
    }
}
