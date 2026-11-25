package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "175")
public class BarbarianLunatic extends Card {

    public BarbarianLunatic() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), DealDamageEffect.toTargetCreature(2)),
                "{2}{R}, Sacrifice Barbarian Lunatic: It deals 2 damage to target creature."
        ));
    }
}
