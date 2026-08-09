package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "NEM", collectorNumber = "80")
public class FlameRift extends Card {

    public FlameRift() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(4, DamageRecipient.EACH_PLAYER));
    }
}
