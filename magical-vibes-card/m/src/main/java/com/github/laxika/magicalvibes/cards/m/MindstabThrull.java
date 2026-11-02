package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "178")
@CardRegistration(set = "FEM", collectorNumber = "40a")
@CardRegistration(set = "FEM", collectorNumber = "40b")
@CardRegistration(set = "FEM", collectorNumber = "40c")
@CardRegistration(set = "ME1", collectorNumber = "76")
public class MindstabThrull extends Card {

    public MindstabThrull() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it.
        // If you do, defending player discards three cards. The defending player is baked onto the
        // trigger's targetId by the unblocked-attack collector, so TARGET_PLAYER reads it.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(new SacrificeSelfThenEffect(new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER)),
                        "You may sacrifice it. If you do, defending player discards three cards."));
    }
}
