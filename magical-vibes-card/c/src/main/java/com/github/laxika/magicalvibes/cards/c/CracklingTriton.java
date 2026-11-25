package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "45")
public class CracklingTriton extends Card {

    public CracklingTriton() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), DealDamageEffect.toAnyTarget(2)),
                "{2}{R}, Sacrifice Crackling Triton: It deals 2 damage to any target."
        ));
    }
}
