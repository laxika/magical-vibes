package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ECL", collectorNumber = "90")
public class BlightedBlackthorn extends Card {

    public BlightedBlackthorn() {
        MayEffect blight = new MayEffect(
                new BlightEffect(2, SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))),
                "Blight 2?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, blight);
        addEffect(EffectSlot.ON_ATTACK, blight);
    }
}
