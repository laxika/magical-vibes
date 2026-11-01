package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Flying and phasing are keyword-driven. The phase-in trigger targets a creature to phase out;
 * targeting is chosen when the ability is put on the stack after the untap-step phasing action.
 */
@CardRegistration(set = "VIS", collectorNumber = "42")
public class ShimmeringEfreet extends Card {

    public ShimmeringEfreet() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.phasesIn(null, ObjectRelation.THIS), new PhaseOutEffect(PhaseOutSubject.TARGET));
    }
}
