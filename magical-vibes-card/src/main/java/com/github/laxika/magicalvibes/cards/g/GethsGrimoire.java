package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "DST", collectorNumber = "123")
public class GethsGrimoire extends Card {

    public GethsGrimoire() {
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
