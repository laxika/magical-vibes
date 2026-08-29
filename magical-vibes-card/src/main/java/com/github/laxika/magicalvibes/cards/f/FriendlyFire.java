package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect;

@CardRegistration(set = "FRF", collectorNumber = "101")
public class FriendlyFire extends Card {

    public FriendlyFire() {
        addEffect(EffectSlot.SPELL,
                new RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new EventValue()));
        addEffect(EffectSlot.SPELL,
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
