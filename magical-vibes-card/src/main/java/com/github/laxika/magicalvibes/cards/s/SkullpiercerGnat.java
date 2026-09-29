package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffect;

@CardRegistration(set = "YONE", collectorNumber = "28")
public class SkullpiercerGnat extends Card {

    public SkullpiercerGnat() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffect());
    }
}
