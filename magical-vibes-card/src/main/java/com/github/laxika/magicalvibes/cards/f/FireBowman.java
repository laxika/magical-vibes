package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "112")
public class FireBowman extends Card {

    public FireBowman() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                "Sacrifice this creature: It deals 1 damage to any target. Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
