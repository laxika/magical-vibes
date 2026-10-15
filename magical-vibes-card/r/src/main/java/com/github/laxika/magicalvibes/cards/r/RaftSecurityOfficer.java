package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetMatches;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "33")
public class RaftSecurityOfficer extends Card {

    public RaftSecurityOfficer() {
        var lowPowerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(3)
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ReduceActivationCostEffect(new FixedIfTargetMatches(lowPowerCreature, 1, 0)),
                        new TapPermanentsEffect(TapUntapScope.TARGET)
                ),
                "{2}, {T}: Tap target creature. This ability costs {1} less to activate if it targets a creature with power 3 or less.",
                TargetFilters.creature()
        ));
    }
}
