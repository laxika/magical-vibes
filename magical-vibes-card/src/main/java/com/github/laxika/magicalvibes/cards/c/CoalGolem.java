package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "99")
public class CoalGolem extends Card {

    public CoalGolem() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.RED, 3)),
                "{3}, Sacrifice this creature: Add {R}{R}{R}."
        ));
    }
}
