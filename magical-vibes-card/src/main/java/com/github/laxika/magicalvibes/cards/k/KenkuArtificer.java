package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "125")
public class KenkuArtificer extends Card {

    public KenkuArtificer() {
        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
        ));

        target(new PermanentPredicateTargetFilter(noncreatureArtifact,
                "Target must be a noncreature artifact"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.PLUS_ONE_PLUS_ONE, 3, noncreatureArtifact))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new AnimatePermanentsEffect(
                                new Fixed(0), new Fixed(0), List.of(CardSubtype.HOMUNCULUS),
                                Set.of(Keyword.FLYING), null,
                                Set.of(CardType.ARTIFACT, CardType.CREATURE), GrantScope.TARGET,
                                EffectDuration.PERMANENT, noncreatureArtifact));
    }
}
