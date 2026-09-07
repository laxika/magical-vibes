package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "192")
public class LurkingArynx extends Card {

    public LurkingArynx() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new MustBlockSourceEffect(null)),
                "{2}{G}: Target creature blocks this creature this turn if able. Activate only if creatures you control have total power 8 or greater."
        ).withActivationCondition(
                new ControlledCreaturesTotalPowerAtLeast(8),
                "Activate only if creatures you control have total power 8 or greater."
        ));
    }
}
