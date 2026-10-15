package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "97")
public class GoblinDynamo extends Card {

    public GoblinDynamo() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This creature deals 1 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(new XValue())),
                "{X}{R}, {T}, Sacrifice this creature: It deals X damage to any target."
        ));
    }
}
