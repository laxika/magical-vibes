package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "125")
public class GoblinCannon extends Card {

    public GoblinCannon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DealDamageToAnyTargetEffect(1), new SacrificeSelfEffect()),
                "{2}: This artifact deals 1 damage to any target. Sacrifice this artifact."
        ));
    }
}
