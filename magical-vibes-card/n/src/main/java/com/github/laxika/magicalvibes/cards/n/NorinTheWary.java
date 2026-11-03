package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "171")
@CardRegistration(set = "SLD", collectorNumber = "827")
public class NorinTheWary extends Card {

    public NorinTheWary() {
        FlickerEffect flicker = FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false);
        addTrigger(Triggers.playerCastsSpell().anySpell(), flicker);
        addTrigger(Triggers.creatureAttacks(), flicker);
    }
}
