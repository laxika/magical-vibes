package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

@CardRegistration(set = "MH1", collectorNumber = "58")
public class MistSyndicateNaga extends Card {

    public MistSyndicateNaga() {
        addNinjutsu("{2}{U}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new CreateTokenCopyOfSourceEffect());
    }
}
