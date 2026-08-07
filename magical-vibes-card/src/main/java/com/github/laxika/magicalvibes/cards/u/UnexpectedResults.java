package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UnexpectedResultsEffect;

@CardRegistration(set = "GTC", collectorNumber = "203")
public class UnexpectedResults extends Card {

    public UnexpectedResults() {
        addEffect(EffectSlot.SPELL, new UnexpectedResultsEffect());
    }
}
