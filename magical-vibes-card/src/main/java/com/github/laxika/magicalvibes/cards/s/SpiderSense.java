package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "46")
@CardRegistration(set = "SPM", collectorNumber = "284")
@CardRegistration(set = "SPM", collectorNumber = "254")
public class SpiderSense extends Card {

    public SpiderSense() {
        PermanentAllOfPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()));
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{U}"),
                new ReturnPermanentsCost(1, tappedCreature))));

        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.TRIGGERED_ABILITY)),
                "Target must be an instant or sorcery spell, or a triggered ability."));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
