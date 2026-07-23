package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "115")
public class BrineShaman extends Card {

    public BrineShaman() {
        // {T}, Sacrifice a creature: Target creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        new BoostTargetCreatureEffect(2, 2)
                ),
                "{T}, Sacrifice a creature: Target creature gets +2/+2 until end of turn."
        ));

        // {1}{U}{U}, Sacrifice a creature: Counter target creature spell.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(
                        new SacrificeCreatureCost(),
                        new CounterSpellEffect()
                ),
                "{1}{U}{U}, Sacrifice a creature: Counter target creature spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                        "Target must be a creature spell."
                )
        ));
    }
}
