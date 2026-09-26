package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "24")
@CardRegistration(set = "MSC", collectorNumber = "321")
public class RoyalTalonFighterJet extends Card {

    public RoyalTalonFighterJet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        CountersOnSource plusOnePlusOneCounters = new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSoldier(plusOnePlusOneCounters));
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.whiteSoldier(plusOnePlusOneCounters));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
