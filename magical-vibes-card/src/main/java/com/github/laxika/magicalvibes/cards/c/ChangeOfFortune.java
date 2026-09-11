package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "VOW", collectorNumber = "150")
public class ChangeOfFortune extends Card {

    public ChangeOfFortune() {
        addEffect(EffectSlot.SPELL, new DiscardHandEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new CardsDiscardedOrCycledThisTurn()));
    }
}
