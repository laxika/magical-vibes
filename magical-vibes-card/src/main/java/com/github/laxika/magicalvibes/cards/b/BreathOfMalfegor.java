package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "ARB", collectorNumber = "35")
public class BreathOfMalfegor extends Card {

    public BreathOfMalfegor() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(5, DamageRecipient.EACH_OPPONENT));
    }
}
