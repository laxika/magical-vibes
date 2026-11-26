package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "121")
public class LeapOfFlame extends Card {

    public LeapOfFlame() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{U}{R}")));
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 0))
                .addSpellEffect(new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), Subjects.target()));
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{U}{R}")),
                new CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount("{U}{R}")));
    }
}
