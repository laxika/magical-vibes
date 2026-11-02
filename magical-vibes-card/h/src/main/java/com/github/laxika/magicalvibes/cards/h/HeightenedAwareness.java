package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "37")
public class HeightenedAwareness extends Card {

    public HeightenedAwareness() {
        // As this enchantment enters, discard your hand.
        addStaticEffect(new DiscardHandAsEntersEffect());

        // At the beginning of your draw step, draw an additional card.
        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new DrawCardEffect(1));
    }
}
