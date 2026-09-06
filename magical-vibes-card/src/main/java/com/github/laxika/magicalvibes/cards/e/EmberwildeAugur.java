package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "97")
public class EmberwildeAugur extends Card {

    public EmberwildeAugur() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DealDamageToTargetPlayerOrPlaneswalkerEffect(3)),
                "Sacrifice this creature: It deals 3 damage to target player or planeswalker. "
                        + "Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
