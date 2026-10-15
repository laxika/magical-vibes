package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGiveSpellCastLifeLossToRandomNonlandCardInDamagedHandEffect;

@CardRegistration(set = "YOTJ", collectorNumber = "15")
public class WagonWrecker extends Card {

    public WagonWrecker() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PerpetuallyGiveSpellCastLifeLossToRandomNonlandCardInDamagedHandEffect());
    }
}
