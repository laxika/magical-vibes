package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfTargetGroup;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKC", collectorNumber = "31")
@CardRegistration(set = "MKC", collectorNumber = "341")
public class HavocEater extends Card {

    public HavocEater() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        targetUpTo(new Sum(new PlayersInGame(), new Fixed(-1)),
                TargetFilters.creatureAnOpponentControls(), 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GoadTargetCreatureUntilNextTurnEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new TotalPowerOfTargetGroup(0)));
    }
}
