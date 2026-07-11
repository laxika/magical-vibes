package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "10")
public class KongmingsContraptions extends Card {

    public KongmingsContraptions() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "{T}: This creature deals 2 damage to target attacking creature. Activate only during the "
                        + "declare attackers step and only if you've been attacked this step.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAttackingPredicate(),
                        "Target must be an attacking creature"
                ),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_DECLARE_ATTACKERS_IF_ATTACKED));
    }
}
