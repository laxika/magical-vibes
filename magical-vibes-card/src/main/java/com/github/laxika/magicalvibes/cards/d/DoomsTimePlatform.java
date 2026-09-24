package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandCardFromGraveyardWithSuspendEffect;

@CardRegistration(set = "MSC", collectorNumber = "103")
@CardRegistration(set = "MSC", collectorNumber = "431")
public class DoomsTimePlatform extends Card {

    public DoomsTimePlatform() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ExileTargetNonlandCardFromGraveyardWithSuspendEffect(2));
    }
}
