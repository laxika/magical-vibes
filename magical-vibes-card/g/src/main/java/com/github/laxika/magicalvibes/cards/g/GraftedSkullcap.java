package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "298")
@CardRegistration(set = "USG", collectorNumber = "296")
public class GraftedSkullcap extends Card {

    public GraftedSkullcap() {
        // At the beginning of your draw step, draw an additional card.
        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new DrawCardEffect(1));

        // At the beginning of your end step, discard your hand.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DiscardHandEffect());
    }
}
