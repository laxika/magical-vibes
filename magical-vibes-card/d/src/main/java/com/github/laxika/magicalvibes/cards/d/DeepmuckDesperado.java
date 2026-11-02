package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "42")
public class DeepmuckDesperado extends Card {

    public DeepmuckDesperado() {
        addTrigger(Triggers.youCommitCrime(),
                new OncePerTurnTriggerEffect(new MillEffect(3, MillRecipient.EACH_OPPONENT)));
    }
}
