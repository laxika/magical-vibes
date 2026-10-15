package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOC", collectorNumber = "9")
@CardRegistration(set = "EOC", collectorNumber = "29")
public class DepthshakerTitan extends Card {

    public DepthshakerTitan() {
        PermanentPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.MELEE, Keyword.TRAMPLE, Keyword.HASTE),
                GrantScope.ALL_OWN_CREATURES, artifactCreature));

        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())));
        ControlledPermanentPredicateTargetFilter targetFilter =
                new ControlledPermanentPredicateTargetFilter(
                        noncreatureArtifact,
                        "Target must be a noncreature artifact you control");
        target(targetFilter, 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AnimatePermanentsEffect(
                        new Fixed(3), new Fixed(3), List.of(), Set.of(), null, Set.of(CardType.ARTIFACT),
                        GrantScope.TARGET, EffectDuration.PERMANENT, noncreatureArtifact))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new SacrificeTargetPermanentAtEndStepEffect());
    }
}
