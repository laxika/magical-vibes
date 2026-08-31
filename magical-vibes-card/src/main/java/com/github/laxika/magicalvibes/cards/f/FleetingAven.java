package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "ONS", collectorNumber = "83")
public class FleetingAven extends Card {

    public FleetingAven() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CYCLES, ReturnToHandEffect.self());
    }
}
