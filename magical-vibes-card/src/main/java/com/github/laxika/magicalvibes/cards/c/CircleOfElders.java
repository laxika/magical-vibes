package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "176")
public class CircleOfElders extends Card {

    public CircleOfElders() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 3)),
                "{T}: Add {C}{C}{C}. Activate only if creatures you control have total power 8 or greater."
        ).withActivationCondition(
                new ControlledCreaturesTotalPowerAtLeast(8),
                "Activate only if creatures you control have total power 8 or greater."
        ));
    }
}
