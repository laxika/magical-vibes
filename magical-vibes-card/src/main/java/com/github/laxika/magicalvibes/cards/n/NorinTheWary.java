package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "171")
public class NorinTheWary extends Card {

    public NorinTheWary() {
        FlickerEffect flicker = FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false);
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(flicker)));
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, flicker);
    }
}
