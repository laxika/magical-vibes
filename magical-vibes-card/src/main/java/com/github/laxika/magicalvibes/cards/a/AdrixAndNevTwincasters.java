package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;

@CardRegistration(set = "SLD", collectorNumber = "1544")
public class AdrixAndNevTwincasters extends Card {

    public AdrixAndNevTwincasters() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(0, 2));
        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(2));
    }
}
