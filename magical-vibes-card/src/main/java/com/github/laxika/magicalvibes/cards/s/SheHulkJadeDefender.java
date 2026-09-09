package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "188")
public class SheHulkJadeDefender extends Card {

    public SheHulkJadeDefender() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(SequenceEffect.of(
                        new DestroyTargetPermanentEffect(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                )),
                "Power-up — {4}{G}{G}: Destroy up to one target artifact or enchantment. Put a +1/+1 counter on She-Hulk. "
                        + "(Activate each power-up ability only once. Reduce the cost by her mana cost if she entered this turn.)",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment"
                ),
                null,
                null,
                null,
                List.of(),
                0,
                1
        ).withPowerUp());
    }
}
