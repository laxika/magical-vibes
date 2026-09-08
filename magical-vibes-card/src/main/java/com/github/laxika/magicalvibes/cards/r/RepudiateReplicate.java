package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "227")
public class RepudiateReplicate extends Card {

    public RepudiateReplicate() {
        var abilityFilter = new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.ACTIVATED_ABILITY,
                        StackEntryType.TRIGGERED_ABILITY)),
                "Target must be an activated or triggered ability.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Repudiate — Counter target activated or triggered ability",
                        new CounterSpellEffect(),
                        abilityFilter
                ).withManaCost("{G/U}{G/U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Replicate — Create a token that's a copy of target creature you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        TargetFilters.creatureYouControl()
                ).withManaCost("{1}{G}{U}")
        )));
    }
}
