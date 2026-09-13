package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "ANA", collectorNumber = "42")
@CardRegistration(set = "XANA", collectorNumber = "42")
public class OgrePainbringer extends Card {

    public OgrePainbringer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToPlayersEffect(3, DamageRecipient.EACH_PLAYER));
    }
}
