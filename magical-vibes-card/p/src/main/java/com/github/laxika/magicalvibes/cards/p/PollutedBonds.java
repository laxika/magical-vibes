package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "74")
@CardRegistration(set = "WOT", collectorNumber = "34")
@CardRegistration(set = "WOT", collectorNumber = "75")
@CardRegistration(set = "WOT", collectorNumber = "95")
public class PollutedBonds extends Card {

    public PollutedBonds() {
        // Whenever a land an opponent controls enters, that player loses 2 life and you gain 2 life.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS), SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(2)));
    }
}
