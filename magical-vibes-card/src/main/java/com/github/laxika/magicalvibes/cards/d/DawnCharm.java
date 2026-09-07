package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "4")
public class DawnCharm extends Card {

    public DawnCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Prevent all combat damage that would be dealt this turn",
                        PreventDamageEffect.allCombat()),
                new ChooseOneEffect.ChooseOneOption(
                        "Regenerate target creature",
                        new RegenerateEffect(true),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell that targets you",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTargetsYouPredicate(),
                                "Target must be a spell that targets you."))
        )));
    }
}
