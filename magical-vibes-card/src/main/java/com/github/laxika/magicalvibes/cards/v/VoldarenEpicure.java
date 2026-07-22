package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "INR", collectorNumber = "182")
public class VoldarenEpicure extends Card {

    public VoldarenEpicure() {
        // When this creature enters, it deals 1 damage to each opponent. Create a Blood token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));
    }
}
