package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "215")
public class FlamecastWheel extends Card {

    public FlamecastWheel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(3)),
                "{5}, {T}, Sacrifice this artifact: It deals 3 damage to target creature."
        ));
    }
}
