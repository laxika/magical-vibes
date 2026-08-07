package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "WTH", collectorNumber = "69")
public class FledglingDjinn extends Card {

    public FledglingDjinn() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
