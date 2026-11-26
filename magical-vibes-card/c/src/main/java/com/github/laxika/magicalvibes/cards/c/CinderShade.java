package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "240")
public class CinderShade extends Card {

    public CinderShade() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(BoostEffect.self(1, 1)),
                "{B}: This creature gets +1/+1 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), DealDamageEffect.toTargetCreature(new SourcePower())),
                "{R}, Sacrifice this creature: It deals damage equal to its power to target creature.",
                TargetFilters.creature()
        ));
    }
}
