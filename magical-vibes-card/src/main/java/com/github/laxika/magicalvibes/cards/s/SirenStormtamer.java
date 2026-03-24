package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "79")
public class SirenStormtamer extends Card {

    public SirenStormtamer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "{U}, Sacrifice Siren Stormtamer: Counter target spell or ability that targets you or a creature you control.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryHasTargetPredicate(),
                                new StackEntryTargetsYouOrCreatureYouControlPredicate()
                        )),
                        "Target must be a spell or ability that targets you or a creature you control."
                )
        ));
    }
}
