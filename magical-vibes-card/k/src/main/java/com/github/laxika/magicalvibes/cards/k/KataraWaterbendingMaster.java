package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "93")
@CardRegistration(set = "TLE", collectorNumber = "180")
public class KataraWaterbendingMaster extends Card {

    public KataraWaterbendingMaster() {
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn(), new ExperienceCountersEffect(1));

        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(new ControllerExperienceCounters()),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "Draw a card for each experience counter you have?"));
    }
}
