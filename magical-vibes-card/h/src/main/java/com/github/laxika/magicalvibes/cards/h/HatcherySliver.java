package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "741")
@CardRegistration(set = "CMM", collectorNumber = "771")
public class HatcherySliver extends Card {

    public HatcherySliver() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{1}{G}")));
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{1}{G}")),
                new CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount("{1}{G}")));
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.REPLICATE, new CardSubtypePredicate(CardSubtype.SLIVER)));
    }
}
