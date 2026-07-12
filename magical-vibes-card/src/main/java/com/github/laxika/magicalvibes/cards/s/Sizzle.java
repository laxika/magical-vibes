package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "224")
public class Sizzle extends Card {

    public Sizzle() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
    }
}
