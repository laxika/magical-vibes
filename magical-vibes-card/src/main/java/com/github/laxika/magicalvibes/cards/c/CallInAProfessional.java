package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;

@CardRegistration(set = "SNC", collectorNumber = "103")
public class CallInAProfessional extends Card {

    public CallInAProfessional() {
        addEffect(EffectSlot.SPELL, new PlayersCantGainLifeThisTurnEffect());
        addEffect(EffectSlot.SPELL, new DamageCantBePreventedThisTurnEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
