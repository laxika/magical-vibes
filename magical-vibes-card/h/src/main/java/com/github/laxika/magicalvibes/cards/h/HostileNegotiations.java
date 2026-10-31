package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.HostileNegotiationsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "BRO", collectorNumber = "105")
public class HostileNegotiations extends Card {

    public HostileNegotiations() {
        addSpellEffect(new HostileNegotiationsEffect());
        addSpellEffect(new LoseLifeEffect(3, LoseLifeRecipient.CONTROLLER));
    }
}
