package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsOrPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;

@CardRegistration(set = "MSH", collectorNumber = "235")
public class TitaniaRuggedRumbler extends Card {

    public TitaniaRuggedRumbler() {
        addEffect(EffectSlot.SPELL, new DiscardCardOrPayManaCost("{2}"));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessDiscardsOrPaysEffect(2));
    }
}
