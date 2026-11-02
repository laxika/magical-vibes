package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActive;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "35")
@CardRegistration(set = "MIC", collectorNumber = "73")
public class CurseOfObsession extends Card {

    public CurseOfObsession() {
        // At the beginning of enchanted player's draw step, that player draws two additional cards.
        addTrigger(Triggers.drawStep(ActivePlayer.ENCHANTED_PLAYER), new DrawCardForTargetPlayerEffect(2));

        // At the beginning of enchanted player's end step, that player discards their hand.
        addTrigger(Triggers.endStep(ActivePlayer.ENCHANTED_PLAYER),
                new ConditionalEffect(new TargetPlayerIsActive(),
                        new DiscardHandEffect(DiscardRecipient.TARGET_PLAYER)));
    }
}
