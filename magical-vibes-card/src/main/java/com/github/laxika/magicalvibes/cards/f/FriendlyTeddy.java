package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "DSK", collectorNumber = "247")
public class FriendlyTeddy extends Card {

    public FriendlyTeddy() {
        addEffect(EffectSlot.ON_DEATH, new EachPlayerDrawsCardEffect(1));
    }
}
