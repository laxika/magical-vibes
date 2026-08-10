package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "182")
public class GraniteShard extends Card {

    public GraniteShard() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{3}, {T}: This artifact deals 1 damage to any target."));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{R}, {T}: This artifact deals 1 damage to any target."));
    }
}
