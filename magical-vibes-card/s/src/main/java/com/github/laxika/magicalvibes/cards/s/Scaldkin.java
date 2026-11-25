package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "52")
public class Scaldkin extends Card {

    public Scaldkin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), DealDamageEffect.toAnyTarget(2)),
                "{2}{R}, Sacrifice this creature: It deals 2 damage to any target."
        ));
    }
}
