package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "55")
public class Hateflayer extends Card {

    public Hateflayer() {
        // {2}{R}, {Q}: This creature deals damage equal to its power to any target.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{R}",
                List.of(new DealDamageToAnyTargetEffect(new SourcePower())),
                "{2}{R}, {Q}: This creature deals damage equal to its power to any target."
        ).withRequiresUntap());
    }
}
