package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithHighestLifeWinsOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "5")
public class CelestialConvergence extends Card {

    public CelestialConvergence() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.OMEN, new Fixed(7)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new RemoveCounterFromSourceEffect(CounterType.OMEN, 1),
                new ConditionalEffect(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.OMEN)),
                        new PlayerWithHighestLifeWinsOrDrawEffect())));
    }
}
