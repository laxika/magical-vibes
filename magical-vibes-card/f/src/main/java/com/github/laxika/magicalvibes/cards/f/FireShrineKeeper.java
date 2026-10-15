package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "144")
public class FireShrineKeeper extends Card {

    public FireShrineKeeper() {
        // {7}{R}, {T}, Sacrifice Fire Shrine Keeper: It deals 3 damage to each of up to two target creatures.
        addActivatedAbility(new ActivatedAbility(
                true,  // requiresTap
                "{7}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(3)),
                "{7}{R}, {T}, Sacrifice Fire Shrine Keeper: It deals 3 damage to each of up to two target creatures.",
                List.of(
                        TargetFilters.creature(),
                        TargetFilters.creature()
                ),
                0,  // minTargets (up to two)
                2   // maxTargets
        ));
    }
}
