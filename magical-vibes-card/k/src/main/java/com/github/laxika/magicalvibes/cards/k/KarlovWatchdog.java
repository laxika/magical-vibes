package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTurnedFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "20")
public class KarlovWatchdog extends Card {

    public KarlovWatchdog() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new CantBeTurnedFaceUpEffect(Subjects.controlledBy(Subjects.eachOpponent()))));
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3)),
                new BoostAllOwnCreaturesEffect(1, 1));
    }
}
