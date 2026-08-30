package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesBlockingOrBlockedByTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "158")
public class TrialError extends Card {

    public TrialError() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Trial - Return all creatures blocking or blocked by target creature to their owner's hand",
                        new ReturnCreaturesBlockingOrBlockedByTargetEffect(),
                        TargetFilters.creature()).withManaCost("{W}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Error - Counter target multicolored spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                multicoloredSpellPredicate(),
                                "Target spell must be multicolored.")).withManaCost("{U}{B}")
        )));
    }

    private static StackEntryPredicate multicoloredSpellPredicate() {
        List<CardColor> colors = List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        List<StackEntryPredicate> colorPairs = new ArrayList<>();
        for (int first = 0; first < colors.size(); first++) {
            for (int second = first + 1; second < colors.size(); second++) {
                colorPairs.add(new StackEntryAllOfPredicate(List.of(
                        new StackEntryColorInPredicate(EnumSet.of(colors.get(first))),
                        new StackEntryColorInPredicate(EnumSet.of(colors.get(second)))
                )));
            }
        }
        return new StackEntryAnyOfPredicate(colorPairs);
    }
}
