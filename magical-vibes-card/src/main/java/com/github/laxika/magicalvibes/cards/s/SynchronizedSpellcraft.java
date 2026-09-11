package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "168")
public class SynchronizedSpellcraft extends Card {

    public SynchronizedSpellcraft() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
        addEffect(EffectSlot.SPELL,
                new DealDamageToPlayersEffect(new PartySize(), DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
