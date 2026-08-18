package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessFromCombatOpponentEffect;

@CardRegistration(set = "SOK", collectorNumber = "55")
public class ShapeStealer extends Card {

    public ShapeStealer() {
        addEffect(EffectSlot.ON_BLOCK, new SetSelfBasePowerToughnessFromCombatOpponentEffect());
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new SetSelfBasePowerToughnessFromCombatOpponentEffect(), TriggerMode.PER_BLOCKER);
    }
}
