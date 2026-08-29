package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "55")
public class TrapRunner extends Card {

    public TrapRunner() {
        // {T}: Target unblocked attacking creature becomes blocked. Activate only during combat
        // after blockers are declared.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MakeTargetAttackingCreatureBlockedEffect()),
                "{T}: Target unblocked attacking creature becomes blocked. Activate only during combat "
                        + "after blockers are declared.",
                TargetFilters.unblockedAttackingCreature(),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS
        ));
    }
}
