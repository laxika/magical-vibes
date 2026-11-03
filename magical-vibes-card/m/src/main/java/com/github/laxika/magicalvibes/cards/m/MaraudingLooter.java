package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "225")
public class MaraudingLooter extends Card {

    public MaraudingLooter() {
        // Raid — At the beginning of your end step, if you attacked this turn,
        // you may draw a card. If you do, discard a card.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Raid()),
                new MayEffect(SequenceEffect.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)), "Draw a card and discard a card?"));
    }
}
