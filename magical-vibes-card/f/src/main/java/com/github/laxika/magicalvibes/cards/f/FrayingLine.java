package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "257")
public class FrayingLine extends Card {

    public FrayingLine() {
        // When this artifact enters, put a rope counter on target creature you control.
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new PutCounterOnTargetPermanentEffect(CounterType.ROPE));

        // Each upkeep, the active player may pay {2} to put a rope counter on one of their
        // creatures. Otherwise, exile this artifact and every creature without a rope counter,
        // then remove all rope counters from the creatures that remain.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new ForcedCostOrElseEffect(
                new PayManaCost("{2}"),
                List.of(
                        new ExileSelfEffect(),
                        new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.ROPE))))),
                        new RemoveAllCountersFromMatchingPermanentsEffect(CounterType.ROPE,
                                new PermanentIsCreaturePredicate())),
                true,
                false,
                true,
                false,
                List.of(new PutCounterOnTargetPermanentEffect(
                        CounterType.ROPE, 1, new PermanentIsCreaturePredicate()))));
    }
}
