package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect;

@CardRegistration(set = "APC", collectorNumber = "24")
public class IceCave extends Card {

    public IceCave() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect());
    }
}
