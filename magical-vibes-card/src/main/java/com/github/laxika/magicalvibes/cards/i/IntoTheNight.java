package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeNightEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;

@CardRegistration(set = "VOW", collectorNumber = "163")
public class IntoTheNight extends Card {

    public IntoTheNight() {
        addEffect(EffectSlot.SPELL, new BecomeNightEffect());
        addEffect(EffectSlot.SPELL, new DiscardUpToThenDrawThatManyEffect(
                DiscardUpToThenDrawThatManyEffect.ANY_NUMBER, 1));
    }
}
