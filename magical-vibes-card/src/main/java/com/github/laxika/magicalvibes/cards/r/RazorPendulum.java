package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerIfLifeAtMostEffect;

@CardRegistration(set = "MIR", collectorNumber = "317")
public class RazorPendulum extends Card {

    public RazorPendulum() {
        // At the beginning of each player's end step, if that player has 5 or less life,
        // this artifact deals 2 damage to that player.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new DealDamageToEndStepPlayerIfLifeAtMostEffect(2, 5));
    }
}
