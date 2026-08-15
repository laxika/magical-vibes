package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

public class DronepackKindred extends Card {

    public DronepackKindred() {
        addActivatedAbility(new ActivatedAbility(
                false, "{1}", List.of(new BoostSelfEffect(1, 0)),
                "{1}: This creature gets +1/+0 until end of turn."
        ));
    }
}
