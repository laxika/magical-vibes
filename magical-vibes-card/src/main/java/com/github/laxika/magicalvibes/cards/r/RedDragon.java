package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "AFR", collectorNumber = "160")
public class RedDragon extends Card {

    public RedDragon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OPPONENT));
    }
}
