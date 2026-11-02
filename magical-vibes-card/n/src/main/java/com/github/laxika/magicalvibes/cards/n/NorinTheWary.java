package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "171")
@CardRegistration(set = "SLD", collectorNumber = "827")
public class NorinTheWary extends Card {

    public NorinTheWary() {
        FlickerEffect flicker = FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false);
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(null, List.of(flicker)));
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, flicker);
    }
}
