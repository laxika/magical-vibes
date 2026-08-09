package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "233")
public class ExplosiveApparatus extends Card {

    public ExplosiveApparatus() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(2)),
                "{3}, {T}, Sacrifice this artifact: It deals 2 damage to any target."
        ));
    }
}
