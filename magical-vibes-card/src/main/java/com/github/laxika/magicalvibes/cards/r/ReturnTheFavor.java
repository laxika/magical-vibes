package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "142")
public class ReturnTheFavor extends Card {

    public ReturnTheFavor() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{1}")));
        setAllowSharedTargets(true);

        StackEntryPredicate copyTypes = new StackEntryTypeInPredicate(Set.of(
                StackEntryType.INSTANT_SPELL,
                StackEntryType.SORCERY_SPELL,
                StackEntryType.ACTIVATED_ABILITY,
                StackEntryType.TRIGGERED_ABILITY));
        StackEntryPredicate singleTarget = new StackEntryAllOfPredicate(List.of(
                new StackEntryIsSingleTargetPredicate(), copyTypes));

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Copy target instant spell, sorcery spell, activated ability, or triggered ability",
                        new CopyTargetSpellOrAbilityEffect(copyTypes),
                        new StackEntryPredicateTargetFilter(
                                copyTypes, "Target must be an instant or sorcery spell or an ability.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Change the target of target spell or ability with a single target",
                        new ChangeTargetOfTargetSpellWithSingleTargetEffect(),
                        new StackEntryPredicateTargetFilter(
                                singleTarget, "Target must have a single target."))
        )));
    }
}
