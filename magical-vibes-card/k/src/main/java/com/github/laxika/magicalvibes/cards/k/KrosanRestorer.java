package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "130")
@CardRegistration(set = "DMR", collectorNumber = "168")
public class KrosanRestorer extends Card {

    public KrosanRestorer() {
        // {T}: Untap target land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(Subjects.target(), new PermanentIsLandPredicate())),
                "{T}: Untap target land.",
                TargetFilters.land()));

        // Threshold — {T}: Untap up to three target lands. Activate only if there are seven or more
        // cards in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(Subjects.allTargets())),
                "Threshold — {T}: Untap up to three target lands. Activate only if there are seven "
                        + "or more cards in your graveyard.",
                List.of(TargetFilters.land(), TargetFilters.land(), TargetFilters.land()),
                0,
                3
        ).withActivationCondition(
                new GraveyardCardThreshold(7, null),
                "Activate only if there are seven or more cards in your graveyard."));
    }
}
