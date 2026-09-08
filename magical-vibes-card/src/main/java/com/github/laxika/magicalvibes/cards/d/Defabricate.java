package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "45")
public class Defabricate extends Card {

    public Defabricate() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target artifact or enchantment spell",
                        new CounterSpellEffect(CounteredSpellDestination.EXILE),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryAllOfPredicate(List.of(
                                        new StackEntryCardTypeInPredicate(
                                                Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT)),
                                        new StackEntryNotPredicate(new StackEntryTypeInPredicate(
                                                Set.of(StackEntryType.ACTIVATED_ABILITY,
                                                        StackEntryType.TRIGGERED_ABILITY))))),
                                "Target must be an artifact or enchantment spell.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target activated or triggered ability",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                "Target must be an activated or triggered ability."))
        )));
    }
}
