package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "BOK", collectorNumber = "100")
public class FirstVolley extends Card {

    public FirstVolley() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
