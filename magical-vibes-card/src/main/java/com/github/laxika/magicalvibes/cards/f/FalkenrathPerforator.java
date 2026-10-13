package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "MID", collectorNumber = "136")
@CardRegistration(set = "DBL", collectorNumber = "136")
public class FalkenrathPerforator extends Card {

    public FalkenrathPerforator() {
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToPlayersEffect(1, DamageRecipient.DEFENDING_PLAYER));
    }
}
