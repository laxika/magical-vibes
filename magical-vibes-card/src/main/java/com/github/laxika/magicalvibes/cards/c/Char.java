package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "RAV", collectorNumber = "117")
public class Char extends Card {

    public Char() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
    }
}
