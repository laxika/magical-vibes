package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "189")
public class VesselOfVolatility extends Card {

    public VesselOfVolatility() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.RED, 4)),
                "{1}{R}, Sacrifice this enchantment: Add {R}{R}{R}{R}."
        ));
    }
}
