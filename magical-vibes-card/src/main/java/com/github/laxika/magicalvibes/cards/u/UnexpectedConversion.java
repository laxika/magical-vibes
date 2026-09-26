package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSameNameCardsFromHandAndLibraryThenSeekEffect;

@CardRegistration(set = "YMID", collectorNumber = "13")
public class UnexpectedConversion extends Card {

    public UnexpectedConversion() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new ExileSameNameCardsFromHandAndLibraryThenSeekEffect());
    }
}
