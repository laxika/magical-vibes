package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "108")
public class SpikeshotGoblin extends Card {

    public SpikeshotGoblin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DealDamageToAnyTargetEffect(new SourcePower())),
                "{R}, {T}: This creature deals damage equal to its power to any target."
        ));
    }
}
