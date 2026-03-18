package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentWhileSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "215")
public class OliviaVoldaren extends Card {

    public OliviaVoldaren() {
        // {1}{R}: Olivia Voldaren deals 1 damage to another target creature.
        // That creature becomes a Vampire in addition to its other types.
        // Put a +1/+1 counter on Olivia Voldaren.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{R}",
                List.of(
                        new DealDamageToTargetCreatureEffect(1),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.VAMPIRE),
                        new PutCounterOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}{R}: Olivia Voldaren deals 1 damage to another target creature. That creature becomes a Vampire in addition to its other types. Put a +1/+1 counter on Olivia Voldaren.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another creature")));

        // {3}{B}{B}: Gain control of target Vampire for as long as you control Olivia Voldaren.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{B}{B}",
                List.of(new GainControlOfTargetPermanentWhileSourceEffect()),
                "{3}{B}{B}: Gain control of target Vampire for as long as you control Olivia Voldaren.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE))),
                        "Target must be a Vampire creature")));
    }
}
