package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "58")
public class ForensicResearcher extends Card {

    public ForensicResearcher() {
        PermanentPredicate anotherPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, anotherPermanent)),
                "{T}: Untap another target permanent you control.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                        "Target must be another permanent you control")));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new CollectEvidenceCost(3),
                        new TapPermanentsEffect(TapUntapScope.TARGET,
                                TargetFilters.creatureAnOpponentControls().predicate())
                ),
                "{T}, Collect evidence 3: Tap target creature you don't control.",
                TargetFilters.creatureAnOpponentControls()));
    }
}
