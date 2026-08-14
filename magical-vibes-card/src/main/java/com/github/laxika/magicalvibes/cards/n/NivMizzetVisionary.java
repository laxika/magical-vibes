package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FDN", collectorNumber = "123")
public class NivMizzetVisionary extends Card {

    public NivMizzetVisionary() {
        addEffect(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE,
                new DrawCardEffect(new EventValue()));
    }
}
