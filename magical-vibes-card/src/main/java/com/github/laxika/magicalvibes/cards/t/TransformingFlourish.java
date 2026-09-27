package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "39")
@CardRegistration(set = "TDC", collectorNumber = "79")
public class TransformingFlourish extends Card {

    public TransformingFlourish() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be an artifact or creature an opponent controls"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                EventStat.NONE,
                new ExileTopUntilNonlandMayCastWithoutPayingManaCostEffect(),
                ThenEffectRecipient.TARGET_CONTROLLER,
                null,
                false,
                true
        ));
        addEffect(EffectSlot.ON_SELF_CAST,
                new MayEffect(new DemonstrateEffect(), "Copy Transforming Flourish?"));
    }
}
