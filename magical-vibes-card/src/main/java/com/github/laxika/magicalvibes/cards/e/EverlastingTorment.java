package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllDamageDealtWithWitherEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "186")
public class EverlastingTorment extends Card {

    public EverlastingTorment() {
        addEffect(EffectSlot.STATIC, new PlayersCantGainLifeEffect());
        addEffect(EffectSlot.STATIC, new DamageCantBePreventedEffect());
        addEffect(EffectSlot.STATIC, new AllDamageDealtWithWitherEffect());
    }
}
