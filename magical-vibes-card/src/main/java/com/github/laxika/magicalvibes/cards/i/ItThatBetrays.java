package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSacrificedCardUnderControlEffect;

@CardRegistration(set = "ROE", collectorNumber = "7")
public class ItThatBetrays extends Card {

    public ItThatBetrays() {
        addEffect(EffectSlot.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED,
                new ReturnSacrificedCardUnderControlEffect());
    }
}
