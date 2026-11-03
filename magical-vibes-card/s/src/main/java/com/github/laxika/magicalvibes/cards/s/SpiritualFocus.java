package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "49")
public class SpiritualFocus extends Card {

    public SpiritualFocus() {
        addTrigger(Triggers.opponentCausesYouToDiscardCard(),
                SequenceEffect.of(
                        new GainLifeEffect(2),
                        new MayEffect(new DrawCardEffect(), "Draw a card?")));
    }
}
