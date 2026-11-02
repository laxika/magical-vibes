package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandCardFromGraveyardWithSuspendEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "103")
@CardRegistration(set = "MSC", collectorNumber = "431")
public class DoomsTimePlatform extends Card {

    public DoomsTimePlatform() {
        addTrigger(Triggers.youAttack(),
                new ExileTargetNonlandCardFromGraveyardWithSuspendEffect(2));
    }
}
