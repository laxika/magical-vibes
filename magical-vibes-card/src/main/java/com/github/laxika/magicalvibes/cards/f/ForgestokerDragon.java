package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "98")
public class ForgestokerDragon extends Card {

    public ForgestokerDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DealDamageToTargetCreatureEffect(1), new CantBlockThisCombatEffect()),
                "{1}{R}: Forgestoker Dragon deals 1 damage to target creature. That creature can't block this combat. "
                        + "Activate only if Forgestoker Dragon is attacking.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.ONLY_WHILE_ATTACKING
        ));
    }
}
