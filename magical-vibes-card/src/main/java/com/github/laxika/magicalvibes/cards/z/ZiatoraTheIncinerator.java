package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;

@CardRegistration(set = "SNC", collectorNumber = "231")
public class ZiatoraTheIncinerator extends Card {

    public ZiatoraTheIncinerator() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(
                        new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(
                                CreateTokenEffect.ofTreasureToken(3)),
                        "Sacrifice another creature?"));
    }
}
