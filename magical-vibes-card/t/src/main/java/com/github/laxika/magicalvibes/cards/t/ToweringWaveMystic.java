package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "THB", collectorNumber = "77")
public class ToweringWaveMystic extends Card {

    public ToweringWaveMystic() {
        // Whenever this creature deals damage, target player mills that many cards.
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE,
                new MillEffect(new EventValue(), MillRecipient.TARGET_PLAYER));
    }
}
