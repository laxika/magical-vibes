package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "107")
public class StingingBarrier extends Card {

    public StingingBarrier() {
        addActivatedAbility(new ActivatedAbility(true, "{U}", List.of(new DealDamageToAnyTargetEffect(1)),
                "{U}, {T}: Stinging Barrier deals 1 damage to any target."));
    }
}
