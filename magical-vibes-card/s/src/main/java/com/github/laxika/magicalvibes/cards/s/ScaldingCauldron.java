package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "229")
public class ScaldingCauldron extends Card {

    public ScaldingCauldron() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(3)),
                "{3}, {T}, Sacrifice Scalding Cauldron: It deals 3 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
