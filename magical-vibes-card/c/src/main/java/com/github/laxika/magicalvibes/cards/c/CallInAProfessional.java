package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;

@CardRegistration(set = "SNC", collectorNumber = "103")
public class CallInAProfessional extends Card {

    public CallInAProfessional() {
        addSpellEffect(new PlayersCantGainLifeThisTurnEffect());
        addSpellEffect(new DamageCantBePreventedThisTurnEffect());
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
    }
}
