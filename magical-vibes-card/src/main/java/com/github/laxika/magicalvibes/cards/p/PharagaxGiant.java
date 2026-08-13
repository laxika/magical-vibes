package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;

@CardRegistration(set = "BNG", collectorNumber = "104")
public class PharagaxGiant extends Card {

    public PharagaxGiant() {
        addEffect(EffectSlot.STATIC, new TributeEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TributeNotPaidEffect(
                new DealDamageToPlayersEffect(5, DamageRecipient.EACH_OPPONENT)));
    }
}
