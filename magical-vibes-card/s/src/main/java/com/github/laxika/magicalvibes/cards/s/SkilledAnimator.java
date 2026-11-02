package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "73")
public class SkilledAnimator extends Card {

    public SkilledAnimator() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact you control"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new AnimatePermanentsEffect(
                                5, 5, List.of(), Set.of(), null, Set.of(CardType.ARTIFACT, CardType.CREATURE),
                                GrantScope.TARGET, EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD));
    }
}
