package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "152")
public class VengefulDevil extends Card {

    public VengefulDevil() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "Morbid — {T}: This creature deals 1 damage to any target. Activate only if a creature died this turn.",
                ActivationTimingRestriction.MORBID
        ));
    }
}
