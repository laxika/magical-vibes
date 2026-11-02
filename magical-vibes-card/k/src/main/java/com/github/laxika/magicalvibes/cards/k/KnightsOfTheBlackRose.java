package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerWasMonarchAtTurnStart;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "332")
public class KnightsOfTheBlackRose extends Card {

    public KnightsOfTheBlackRose() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());
        addTrigger(Triggers.opponentBecomesMonarch(),
                new ConditionalEffect(
                        new ControllerWasMonarchAtTurnStart(),
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER),
                                new GainLifeEffect(2))));
    }
}
