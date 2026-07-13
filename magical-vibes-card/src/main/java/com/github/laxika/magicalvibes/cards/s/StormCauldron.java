package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTappedLandToHandEffect;

@CardRegistration(set = "7ED", collectorNumber = "320")
public class StormCauldron extends Card {

    public StormCauldron() {
        // Each player may play an additional land during each of their turns.
        addEffect(EffectSlot.STATIC, new EachPlayerPlaysAdditionalLandEffect());
        // Whenever a land is tapped for mana, return it to its owner's hand.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new ReturnTappedLandToHandEffect());
    }
}
