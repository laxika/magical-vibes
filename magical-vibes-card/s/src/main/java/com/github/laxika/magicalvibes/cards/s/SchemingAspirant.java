package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "107")
public class SchemingAspirant extends Card {

    public SchemingAspirant() {
        addTrigger(Triggers.youProliferate(), new SequenceEffect(List.of(
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(2))));
    }
}
