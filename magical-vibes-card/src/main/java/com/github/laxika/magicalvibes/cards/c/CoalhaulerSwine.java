package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "RAV", collectorNumber = "119")
public class CoalhaulerSwine extends Card {

    public CoalhaulerSwine() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_PLAYER));
    }
}
