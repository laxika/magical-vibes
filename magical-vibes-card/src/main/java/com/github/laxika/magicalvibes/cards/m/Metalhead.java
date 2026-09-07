package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "44")
public class Metalhead extends Card {

    public Metalhead() {
        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        artifactOrCreature,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "Target must be another artifact or creature"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnToHandEffect.target());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "another artifact"),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new GrantKeywordEffect(Set.of(Keyword.MENACE, Keyword.HASTE), GrantScope.SELF)
                ),
                "{R}, Sacrifice another artifact: Put a +1/+1 counter on Metalhead. It gains menace and haste until end of turn."
        ));
    }
}
