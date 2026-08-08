package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "103")
public class Frostling extends Card {

    public Frostling() {
        // Sacrifice this creature: It deals 1 damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(1)),
                "Sacrifice Frostling: It deals 1 damage to target creature."
        ));
    }
}
