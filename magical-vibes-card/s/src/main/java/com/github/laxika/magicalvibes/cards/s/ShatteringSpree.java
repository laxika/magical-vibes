package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "75")
@CardRegistration(set = "GK1", collectorNumber = "34")
@CardRegistration(set = "TLE", collectorNumber = "36")
public class ShatteringSpree extends Card {

    public ShatteringSpree() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{R}")));
        target(TargetFilters.artifact()).addSpellEffect(new DestroyTargetPermanentEffect());
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{R}")),
                new CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount("{R}")));
    }
}
