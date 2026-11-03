package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "202")
public class SabertoothMauler extends Card {

    public SabertoothMauler() {
        // At the beginning of your end step, if a creature died this turn, put a +1/+1 counter
        // on this creature and untap it.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Morbid()),
                SequenceEffect.of(
                                new PutCountersOnSourceEffect(1, 1, 1),
                                new UntapPermanentsEffect(TapUntapScope.SELF)
                        ));
    }
}
