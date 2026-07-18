package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "214")
@CardRegistration(set = "4ED", collectorNumber = "179")
public class BrothersOfFire extends Card {

    public BrothersOfFire() {
        // {1}{R}{R}: This creature deals 1 damage to any target and 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(new DealDamageToAnyTargetEffect(1), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{1}{R}{R}: This creature deals 1 damage to any target and 1 damage to you."
        ));
    }
}
