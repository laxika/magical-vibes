package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ARB", collectorNumber = "116")
public class JundSojourners extends Card {

    public JundSojourners() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new DealDamageToAnyTargetEffect(1),
                "Have Jund Sojourners deal 1 damage to any target?"));

        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(new DealDamageToAnyTargetEffect(1),
                "Have Jund Sojourners deal 1 damage to any target?"));
        addCycling("{2}{R}");
    }
}
