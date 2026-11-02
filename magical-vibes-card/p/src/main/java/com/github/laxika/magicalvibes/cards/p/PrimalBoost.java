package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "277")
@CardRegistration(set = "DMR", collectorNumber = "173")
public class PrimalBoost extends Card {

    public PrimalBoost() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(4, 4));

        addCycling("{2}{G}");
        addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                new BoostTargetCreatureEffect(1, 1), "Give target creature +1/+1 until end of turn?"));
    }
}
