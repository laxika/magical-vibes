package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.effect.DrawCardThenIfNoCardsDrawnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "163")
public class TradeRouteEnvoy extends Card {

    public TradeRouteEnvoy() {
        ControlsPermanent controlsCreatureWithCounter = new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.ANY))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardThenIfNoCardsDrawnEffect(
                new FixedIfCondition(controlsCreatureWithCounter, 1, 0),
                new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
