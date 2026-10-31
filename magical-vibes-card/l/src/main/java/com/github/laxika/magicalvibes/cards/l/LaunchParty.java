package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Launch Party — {3}{B} Instant.
 * As an additional cost to cast this spell, sacrifice a creature.
 * Destroy target creature. Its controller loses 2 life.
 */
@CardRegistration(set = "RTR", collectorNumber = "69")
public class LaunchParty extends Card {

    public LaunchParty() {
        // Destroy resolves first (CR 608.2c); the life loss then reads the destroyed creature's controller
        // from its last known information (CR 608.2h).
        addCastingCost(new SacrificeCreatureCost());
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ))
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
