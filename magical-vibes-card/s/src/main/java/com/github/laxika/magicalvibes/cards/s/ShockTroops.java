package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "223")
@CardRegistration(set = "MMQ", collectorNumber = "212")
public class ShockTroops extends Card {

    public ShockTroops() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), DealDamageEffect.toAnyTarget(2)),
                "Sacrifice Shock Troops: It deals 2 damage to any target."
        ));
    }
}
