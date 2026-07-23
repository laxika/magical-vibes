package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockHighPowerCreaturesUnlessPaysEffect;

@CardRegistration(set = "5ED", collectorNumber = "34")
@CardRegistration(set = "ICE", collectorNumber = "31")
public class Hipparion extends Card {

    public Hipparion() {
        // This creature can't block creatures with power 3 or greater unless you pay {1}.
        // Read at declare-blockers time by CombatBlockService via BlockCostEffect.
        addEffect(EffectSlot.STATIC, new CantBlockHighPowerCreaturesUnlessPaysEffect(3, 1));
    }
}
