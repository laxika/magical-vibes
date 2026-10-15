package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "202")
@CardRegistration(set = "5DN", collectorNumber = "110")
public class ClockOfOmens extends Card {

    public ClockOfOmens() {
        // Tap two untapped artifacts you control: Untap target artifact.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsArtifactPredicate())),
                "Tap two untapped artifacts you control: Untap target artifact.",
                TargetFilters.artifact()));
    }
}
