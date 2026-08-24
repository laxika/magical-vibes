package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAuraAttachedPermanentEffect;

@CardRegistration(set = "OTJ", collectorNumber = "202")
public class ErietteTheBeguiler extends Card {

    public ErietteTheBeguiler() {
        addEffect(EffectSlot.ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT,
                new GainControlOfAuraAttachedPermanentEffect());
    }
}
