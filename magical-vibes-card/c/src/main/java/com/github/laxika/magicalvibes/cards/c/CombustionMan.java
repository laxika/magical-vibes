package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "127")
public class CombustionMan extends Card {

    public CombustionMan() {
        target(TargetFilters.permanent()).addTrigger(Triggers.attacks(),
                UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()),
                        new DamageFromSourceCost(new SourcePower()), new DestroyTargetPermanentEffect()));
    }
}
