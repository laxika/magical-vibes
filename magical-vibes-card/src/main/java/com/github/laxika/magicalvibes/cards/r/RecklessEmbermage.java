package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "212")
public class RecklessEmbermage extends Card {

    public RecklessEmbermage() {
        // {1}{R}: This creature deals 1 damage to any target and 1 damage to itself.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new DealDamageToAnyTargetEffect(1),
                        new DealDamageToSourceEffect(1)
                ),
                "{1}{R}: Reckless Embermage deals 1 damage to any target and 1 damage to itself."
        ));
    }
}
