package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "RAV", collectorNumber = "142")
public class SellSwordBrute extends Card {

    public SellSwordBrute() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
    }
}
