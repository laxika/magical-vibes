package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

public class RodOfRuin extends Card {

    public RodOfRuin() {
        addActivatedAbility(new ActivatedAbility(true, "{3}", List.of(new DealDamageToAnyTargetEffect(1)), true, "{3}, {T}: Rod of Ruin deals 1 damage to any target."));
    }
}
