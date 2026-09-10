package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SCG", collectorNumber = "28")
public class AphettoRunecaster extends Card {

    public AphettoRunecaster() {
        addEffect(EffectSlot.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
