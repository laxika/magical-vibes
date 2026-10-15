package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenCantBlockIfCreatureDamagedEffect;

import java.util.List;

public class BallistaWielder extends Card {

    public BallistaWielder() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new DealDamageToAnyTargetThenCantBlockIfCreatureDamagedEffect(1)),
                "{2}{R}: This creature deals 1 damage to any target. A creature dealt damage this way can't block this turn."
        ));
    }
}
