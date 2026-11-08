package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "72")
@CardRegistration(set = "DDJ", collectorNumber = "20")
@CardRegistration(set = "GK1", collectorNumber = "33")
public class Pyromatics extends Card {

    public Pyromatics() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{1}{R}")));
        addSpellEffect(new DealDamageToAnyTargetEffect(1));
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{1}{R}")),
                new CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount("{1}{R}")));
    }
}
