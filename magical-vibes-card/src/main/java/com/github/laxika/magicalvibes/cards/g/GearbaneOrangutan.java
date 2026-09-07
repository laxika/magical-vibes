package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "129")
public class GearbaneOrangutan extends Card {

    public GearbaneOrangutan() {
        var artifactPredicate = new PermanentIsArtifactPredicate();
        var artifactTargetFilter = new PermanentPredicateTargetFilter(
                artifactPredicate, "Target must be an artifact");
        var artifactTarget = target(artifactTargetFilter, 0, 1);
        var destroyArtifact = new DestroyTargetPermanentEffect(artifactPredicate);
        registerEffectTargetIndex(destroyArtifact, artifactTarget.getIndex());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy up to one target artifact.",
                        List.of(destroyArtifact), artifactTargetFilter, null, 0, 1, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Sacrifice an artifact. If you do, put two +1/+1 counters on this creature.",
                        new SacrificePermanentThenEffect(
                                artifactPredicate,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                                "an artifact", false, false))
        ))));
    }
}
