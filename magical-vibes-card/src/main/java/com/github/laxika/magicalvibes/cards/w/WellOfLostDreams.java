package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaDrawXCardsEffect;

@CardRegistration(set = "DST", collectorNumber = "159")
public class WellOfLostDreams extends Card {

    public WellOfLostDreams() {
        // Whenever you gain life, you may pay {X}, where X is less than or equal to the amount of
        // life you gained. If you do, draw X cards.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PayXManaDrawXCardsEffect());
    }
}
