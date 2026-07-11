package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "PTK", collectorNumber = "75")
public class Famine extends Card {

    public Famine() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(3, DamageRecipient.EACH_PLAYER));
    }
}
