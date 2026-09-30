package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.List;
import java.util.UUID;

/**
 * Tempting offer that creates one copy for the spell's controller, then offers each opponent a
 * copy; an opponent who accepts also gives the spell's controller another copy.
 */
public record TemptingOfferCopySpellEffect(
        StackEntry spellSnapshot,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId
) implements CardEffect {

    public TemptingOfferCopySpellEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    public TemptingOfferCopySpellEffect() {
        this(null, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
