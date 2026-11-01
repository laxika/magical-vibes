package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "141")
@CardRegistration(set = "A25", collectorNumber = "137")
public class IreShaman extends Card {

    public IreShaman() {
        addMorph("{R}");
        addStaticEffect(new PutCountersOnTurnFaceUpEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
