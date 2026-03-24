package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;

@CardRegistration(set = "XLN", collectorNumber = "154")
public class RampagingFerocidon extends Card {

    public RampagingFerocidon() {
        // Players can't gain life.
        addEffect(EffectSlot.STATIC, new PlayersCantGainLifeEffect());
        // Whenever another creature enters, Rampaging Ferocidon deals 1 damage to that creature's controller.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new DealDamageToTargetPlayerEffect(1));
    }
}
