package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "CON", collectorNumber = "74")
public class VolcanicFallout extends Card {

    public VolcanicFallout() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_PLAYER));
    }
}
