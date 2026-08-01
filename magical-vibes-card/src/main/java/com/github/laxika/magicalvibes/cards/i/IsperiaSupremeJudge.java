package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "RTR", collectorNumber = "171")
public class IsperiaSupremeJudge extends Card {

    public IsperiaSupremeJudge() {
        // Whenever a creature attacks you or a planeswalker you control, you may draw a card.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
