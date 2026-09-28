package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "68")
@CardRegistration(set = "NCC", collectorNumber = "168")
public class BrokersConfluence extends Card {

    public BrokersConfluence() {
        setAllowSharedTargets(true);

        var abilityFilter = new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.ACTIVATED_ABILITY,
                        StackEntryType.TRIGGERED_ABILITY)),
                "Target must be an activated or triggered ability.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Proliferate.",
                        new ProliferateEffect()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target creature phases out.",
                        () -> new PhaseOutEffect(PhaseOutSubject.TARGET),
                        TargetFilters.creature()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Counter target activated or triggered ability.",
                        CounterSpellEffect::new,
                        abilityFilter)
        ), 3));
    }
}
