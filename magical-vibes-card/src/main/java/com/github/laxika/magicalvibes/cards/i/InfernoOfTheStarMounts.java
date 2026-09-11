package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfAndTriggerWhenPowerBecomesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "151")
public class InfernoOfTheStarMounts extends Card {

    public InfernoOfTheStarMounts() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new BoostSelfAndTriggerWhenPowerBecomesEffect(1, 0, 20, 20)),
                "{R}: Inferno of the Star Mounts gets +1/+0 until end of turn. When its power becomes 20 this way, it deals 20 damage to any target."));
    }
}
