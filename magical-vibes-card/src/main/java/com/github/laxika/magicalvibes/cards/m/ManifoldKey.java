package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "230")
public class ManifoldKey extends Card {

    public ManifoldKey() {
        PermanentAllOfPredicate anotherArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, anotherArtifact)),
                "{1}, {T}: Untap another target artifact.",
                new PermanentPredicateTargetFilter(anotherArtifact, "Target must be another artifact")
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{3}, {T}: Target creature can't be blocked this turn.",
                TargetFilters.creature()
        ));
    }
}
