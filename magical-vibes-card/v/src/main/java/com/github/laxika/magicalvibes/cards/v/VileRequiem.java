package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "167")
@CardRegistration(set = "C13", collectorNumber = "98")
public class VileRequiem extends Card {

    public VileRequiem() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new PutCountersOnSelfEffect(CounterType.VERSE),
                "Put a verse counter on Vile Requiem?"));

        PermanentPredicateTargetFilter nonblackCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Targets must be nonblack creatures"
        );
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeSelfCost(), new DestroyEachTargetPermanentEffect(true)),
                "{1}{B}, Sacrifice this enchantment: Destroy up to X target nonblack creatures, where X is the number of verse counters on this enchantment. They can't be regenerated.",
                nonblackCreature,
                null,
                null,
                null,
                List.of(),
                0,
                Integer.MAX_VALUE
        ).withSourceCounterScaledTargets(CounterType.VERSE));
    }
}
