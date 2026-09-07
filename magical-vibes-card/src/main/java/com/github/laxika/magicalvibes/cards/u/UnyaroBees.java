package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "231")
public class UnyaroBees extends Card {

    public UnyaroBees() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new BoostSelfEffect(1, 1)),
                "This creature gets +1/+1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(2)),
                "It deals 2 damage to any target."
        ));
    }
}
