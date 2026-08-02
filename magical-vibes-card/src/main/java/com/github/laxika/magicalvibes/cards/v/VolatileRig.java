package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "RTR", collectorNumber = "236")
public class VolatileRig extends Card {

    public VolatileRig() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new FlipCoinWinEffect(null, new SacrificeSelfEffect()));
        addEffect(EffectSlot.ON_DEATH,
                new FlipCoinWinEffect(null, new MassDamageEffect(4, true)));
    }
}
