package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "87")
@CardRegistration(set = "DDT", collectorNumber = "15")
public class Misdirection extends Card {

    public Misdirection() {
        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLUE), "blue"))));
        target(new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY, StackEntryType.TRIGGERED_ABILITY))),
                                new StackEntryIsSingleTargetPredicate())),
                "Target spell must have a single target."
        )).addEffect(EffectSlot.SPELL, new ChangeTargetOfTargetSpellWithSingleTargetEffect());
    }
}
