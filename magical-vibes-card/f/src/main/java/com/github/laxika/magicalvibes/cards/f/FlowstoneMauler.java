package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "85")
@CardRegistration(set = "TPR", collectorNumber = "133")
public class FlowstoneMauler extends Card {

    public FlowstoneMauler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(BoostEffect.self(1, -1)),
                "{R}: This creature gets +1/-1 until end of turn."
        ));
    }
}
