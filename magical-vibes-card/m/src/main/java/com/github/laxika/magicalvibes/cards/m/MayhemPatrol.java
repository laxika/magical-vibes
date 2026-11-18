package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "114")
public class MayhemPatrol extends Card {

    public MayhemPatrol() {
        addCastingOption(AlternateHandCast.blitz("{1}{R}"));

        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new BoostTargetCreatureEffect(1, 0));

        // Blitz (CR 702.152a): haste while the blitz cost was paid; the end-step sacrifice is
        // created as the spell resolves.
    }
}
