package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C13", collectorNumber = "198")
public class MarathWillOfTheWild extends Card {

    public MarathWillOfTheWild() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new ManaSpentToCast()));

        AnyTargetPredicateTargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target");

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put X +1/+1 counters on target creature",
                                        new PutCounterOnTargetPermanentEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, new XValue()),
                                        TargetFilters.creature()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Marath deals X damage to any target",
                                        new DealDamageToAnyTargetEffect(new XValue()),
                                        anyTarget),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Create an X/X green Elemental creature token",
                                        new CreateTokenEffect("Elemental", new XValue(), new XValue(),
                                                CardColor.GREEN, List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of()))))),
                "{X}, Remove X +1/+1 counters from Marath: Choose one — Put X +1/+1 counters on target creature; "
                        + "Marath deals X damage to any target; or create an X/X green Elemental creature token. "
                        + "X can't be 0."
        ).withMinimumXValue(1));
    }
}
