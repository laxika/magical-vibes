package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "201")
public class HeartfireImmolator extends Card {

    public HeartfireImmolator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureOrPlaneswalkerEffect(new SourcePower())),
                "{R}, Sacrifice this creature: It deals damage equal to its power to target creature or planeswalker."
        ));
    }
}
