package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "196")
@CardRegistration(set = "2X2", collectorNumber = "187")
@CardRegistration(set = "C19", collectorNumber = "189")
public class BountyOfTheLuxa extends Card {

    public BountyOfTheLuxa() {
        // At the beginning of your first main phase, remove all flood counters from this
        // enchantment. If no counters were removed this way, put a flood counter on this
        // enchantment and draw a card. Otherwise, add {C}{G}{U}.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), SequenceEffect.of(
                new RemoveCounterFromSourceEffect(CounterType.FLOOD, Integer.MAX_VALUE),
                new ConditionalEffect(new NotCondition(HappenedThisWay.removedCounters()), SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.FLOOD, 1),
                        new DrawCardEffect(1))),
                new ConditionalEffect(HappenedThisWay.removedCounters(), SequenceEffect.of(
                        new AwardManaEffect(ManaColor.COLORLESS),
                        new AwardManaEffect(ManaColor.GREEN),
                        new AwardManaEffect(ManaColor.BLUE)))));
    }
}
