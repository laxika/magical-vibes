package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;

@CardRegistration(set = "10E", collectorNumber = "184")
public class UnderworldDreams extends Card {

    public UnderworldDreams() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new DealDamageToTargetPlayerEffect(1));
    }
}
