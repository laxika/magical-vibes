package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.HostileNegotiationsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "BRO", collectorNumber = "105")
public class HostileNegotiations extends Card {

    public HostileNegotiations() {
        addEffect(EffectSlot.SPELL, new HostileNegotiationsEffect());
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(3, LoseLifeRecipient.CONTROLLER));
    }
}
