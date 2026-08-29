package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "254")
public class BarrelsOfBlastingJelly extends Card {

    public BarrelsOfBlastingJelly() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}: Add one mana of any color. Activate only once each turn.",
                1
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(5)),
                "{5}, {T}, Sacrifice this artifact: It deals 5 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
