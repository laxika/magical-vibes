package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;

@CardRegistration(set = "7ED", collectorNumber = "105")
public class TolarianWinds extends Card {

    public TolarianWinds() {
        addEffect(EffectSlot.SPELL, new DiscardOwnHandThenDrawThatManyEffect());
    }
}
