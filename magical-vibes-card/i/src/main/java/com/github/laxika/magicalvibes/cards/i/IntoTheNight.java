package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeNightEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;

@CardRegistration(set = "VOW", collectorNumber = "163")
@CardRegistration(set = "DBL", collectorNumber = "430")
public class IntoTheNight extends Card {

    public IntoTheNight() {
        addSpellEffect(new BecomeNightEffect());
        addSpellEffect(new DiscardUpToThenDrawThatManyEffect(
                DiscardUpToThenDrawThatManyEffect.ANY_NUMBER, 1));
    }
}
