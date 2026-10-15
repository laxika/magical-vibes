package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.TapEnchantedPermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "97")
public class NaturesChosen extends Card {

    public NaturesChosen() {
        // Enchant creature you control.
        target(TargetFilters.creatureYouControl());

        // {0}: Untap enchanted creature. Activate only during your turn and only once each turn.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new UntapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{0}: Untap enchanted creature. Activate only during your turn and only once each turn.",
                null, null, 1, ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));

        // Tap enchanted creature: Untap target artifact, creature, or land. Activate only if
        // enchanted creature is white and untapped and only once each turn. The tap cost cannot be
        // paid by an already-tapped creature, which covers the printed "untapped" clause.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new TapEnchantedPermanentCost(), new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "Tap enchanted creature: Untap target artifact, creature, or land. Activate only if "
                        + "enchanted creature is white and untapped and only once each turn.",
                new PermanentPredicateTargetFilter(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate())),
                        "Target must be an artifact, creature, or land"),
                null, 1, null)
                .withActivationCondition(
                        new EnchantedPermanentMatches(new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                                "enchanted creature is white"),
                        "Enchanted creature must be white."));
    }
}
