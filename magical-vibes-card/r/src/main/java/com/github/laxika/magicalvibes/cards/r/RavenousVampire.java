package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "136")
public class RavenousVampire extends Card {

    public RavenousVampire() {
        // At the beginning of your upkeep, you may sacrifice a nonartifact creature. If you do, put a
        // +1/+1 counter on this creature. If you don't, tap this creature.
        // It may sacrifice itself (excludeSource false).
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.youMayPay(
                        new SacrificePermanentCost(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsArtifactPredicate()))),
                                "nonartifact creature", false),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE))
                .ifNotPaid(new TapPermanentsEffect(Subjects.source())));
    }
}
