package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "181")
public class NimrodelWatcher extends Card {

    public NimrodelWatcher() {
        addTrigger(Triggers.youScry(), new OncePerTurnTriggerEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new MakeCreatureUnblockableEffect(true))));
    }
}
