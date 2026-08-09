package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "115")
public class SealOfStrength extends Card {

    public SealOfStrength() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(3, 3)),
                "Sacrifice Seal of Strength: Target creature gets +3/+3 until end of turn."
        ));
    }
}
