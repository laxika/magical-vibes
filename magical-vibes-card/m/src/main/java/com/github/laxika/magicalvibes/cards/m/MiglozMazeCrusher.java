package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "210")
public class MiglozMazeCrusher extends Card {

    public MiglozMazeCrusher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OIL, new Fixed(5)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.OIL),
                        new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.MENACE), GrantScope.SELF)
                ),
                "{1}, Remove an oil counter from Migloz: It gains vigilance and menace until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.OIL),
                        new BoostSelfEffect(2, 2)
                ),
                "{2}, Remove two oil counters from Migloz: It gets +2/+2 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.OIL),
                        new DestroyTargetPermanentEffect()
                ),
                "{3}, Remove three oil counters from Migloz: Destroy target artifact or enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment"
                )
        ));
    }
}
