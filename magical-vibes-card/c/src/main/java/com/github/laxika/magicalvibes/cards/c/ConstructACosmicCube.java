package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "90")
public class ConstructACosmicCube extends Card {

    public ConstructACosmicCube() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), SequenceEffect.of(
                new CreateTokenEffect(
                        "Villain",
                        2,
                        1,
                        CardColor.BLACK,
                        List.of(CardSubtype.VILLAIN),
                        Set.of(Keyword.MENACE),
                        Set.of()),
                new PutCountersOnSelfEffect(CounterType.PLAN)));

        addTrigger(Triggers.countersPutOnThis(), TriggerOptions.DEFAULT.withEventCondition(
                new SourceCounterThreshold(7, CounterType.PLAN)),
                SacrificeSelfThenEffect.reflexive(
                        new ControlTargetPlayerNextTurnEffect(PlayerRelation.OPPONENT)));
    }
}
