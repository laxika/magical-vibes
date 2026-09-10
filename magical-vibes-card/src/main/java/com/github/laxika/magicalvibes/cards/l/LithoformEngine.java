package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetActivatedOrTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "245")
public class LithoformEngine extends Card {

    public LithoformEngine() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new CopyTargetActivatedOrTriggeredAbilityEffect()),
                "{2}, {T}: Copy target activated or triggered ability you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryControlledByPredicate())),
                        "Target must be an activated or triggered ability you control.")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new CopySpellEffect()),
                "{3}, {T}: Copy target instant or sorcery spell you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByPredicate())),
                        "Target must be an instant or sorcery spell you control.")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(CopySpellEffect.permanentSpellBecomesToken()),
                "{4}, {T}: Copy target permanent spell you control. (The copy becomes a token.)",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.CREATURE_SPELL,
                                        StackEntryType.ENCHANTMENT_SPELL,
                                        StackEntryType.ARTIFACT_SPELL,
                                        StackEntryType.PLANESWALKER_SPELL,
                                        StackEntryType.BATTLE_SPELL)),
                                new StackEntryControlledByPredicate())),
                        "Target must be a permanent spell you control.")));
    }
}
