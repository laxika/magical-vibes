package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "DFT", collectorNumber = "137")
public class MagmakinArtillerist extends Card {

    public MagmakinArtillerist() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT));
        addCycling("{1}{R}");
    }
}
