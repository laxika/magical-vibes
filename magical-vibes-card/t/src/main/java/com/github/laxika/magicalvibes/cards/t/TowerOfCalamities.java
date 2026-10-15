package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "212")
public class TowerOfCalamities extends Card {

    public TowerOfCalamities() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(new DealDamageToTargetCreatureEffect(12)),
                "{8}, {T}: Tower of Calamities deals 12 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
