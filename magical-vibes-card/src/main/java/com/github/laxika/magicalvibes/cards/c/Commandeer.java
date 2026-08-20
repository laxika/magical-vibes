package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "29")
public class Commandeer extends Card {

    public Commandeer() {
        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLUE), "blue", 2))));

        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                "Target must be a noncreature spell."
        ));
        addEffect(EffectSlot.SPELL, new GainControlOfTargetSpellEffect());
    }
}
