package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "52")
public class BlindZealot extends Card {

    public BlindZealot() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect(),
                        "You may sacrifice it. If you do, destroy target creature that player controls."));
    }
}
