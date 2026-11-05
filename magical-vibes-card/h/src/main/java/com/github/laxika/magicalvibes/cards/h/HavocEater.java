package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfTargetGroup;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "31")
@CardRegistration(set = "MKC", collectorNumber = "341")
public class HavocEater extends Card {

    public HavocEater() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        var goadTargetCreatureUntilNextTurn = new GoadTargetCreatureUntilNextTurnEffect();
        targetUpTo(new Sum(new PlayersInGame(), new Fixed(-1)),
                TargetFilters.creatureAnOpponentControls(), 99)
                .bindEffect(goadTargetCreatureUntilNextTurn);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                goadTargetCreatureUntilNextTurn,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new TotalPowerOfTargetGroup(0)));
    }
}
