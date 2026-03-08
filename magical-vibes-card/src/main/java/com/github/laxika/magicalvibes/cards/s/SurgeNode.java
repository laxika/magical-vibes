package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "160")
public class SurgeNode extends Card {

    public SurgeNode() {
        // This artifact enters with six charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(6));

        // {1}, {T}, Remove a charge counter from Surge Node: Put a charge counter on target artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new PutChargeCounterOnTargetPermanentEffect()
                ),
                "{1}, {T}, Remove a charge counter from Surge Node: Put a charge counter on target artifact.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact."
                )
        ));
    }
}
