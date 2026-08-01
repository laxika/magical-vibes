package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Flying and phasing are keyword-driven. The phase-in trigger targets a creature to phase out;
 * targeting is chosen when the ability is put on the stack after the untap-step phasing action.
 */
@CardRegistration(set = "VIS", collectorNumber = "42")
public class ShimmeringEfreet extends Card {

    public ShimmeringEfreet() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_SELF_PHASES_IN, new PhaseOutTargetPermanentEffect());
    }
}
