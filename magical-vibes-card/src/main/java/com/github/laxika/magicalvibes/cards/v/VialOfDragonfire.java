package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "241")
public class VialOfDragonfire extends Card {

    public VialOfDragonfire() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(2)),
                "{2}, {T}, Sacrifice this artifact: It deals 2 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
