package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "40")
public class HydromorphGull extends Card {

    public HydromorphGull() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "{U}, Sacrifice this creature: Counter target spell that targets a creature you control.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTargetsPermanentPredicate(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be a spell that targets a creature you control."
                )
        ));
    }
}
