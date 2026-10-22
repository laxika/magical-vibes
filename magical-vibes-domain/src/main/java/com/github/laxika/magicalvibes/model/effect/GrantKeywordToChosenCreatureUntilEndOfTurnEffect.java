package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.UUID;

/**
 * Grants a keyword to the creature that was chosen when the source permanent entered
 * the battlefield (via {@code chosenPermanentId}). The {@code chosenCreatureId} is
 * {@code null} in the card definition and baked in during effect snapshotting from
 * the source permanent's {@code chosenPermanentId}. A resolution-time choice may instead be
 * carried by the resolving stack entry.
 *
 * <p>When {@code requiresChosenCreature} is set, the grant never falls back to the controller's only
 * eligible creature: it applies only to a creature chosen earlier in the same resolution (for example
 * the creature a preceding search put onto the battlefield), and does nothing when there is none.
 *
 * <p>Used by cards like Dauntless Bodyguard: "Sacrifice this creature: The chosen
 * creature gains indestructible until end of turn."
 */
public record GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword keyword, UUID chosenCreatureId,
                                                               boolean requiresChosenCreature)
        implements CardEffect {

    public GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword keyword, UUID chosenCreatureId) {
        this(keyword, chosenCreatureId, false);
    }

    @Override
    public boolean usesChosenPermanentReference() {
        return chosenCreatureId == null;
    }
}
