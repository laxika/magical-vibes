package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MBS", collectorNumber = "90")
public class RotWolf extends Card {

    public RotWolf() {
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
