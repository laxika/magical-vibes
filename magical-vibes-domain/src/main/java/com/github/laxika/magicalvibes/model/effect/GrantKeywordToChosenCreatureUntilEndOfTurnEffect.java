package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.UUID;

/**
 * Grants a keyword to the creature that was chosen when the source permanent entered
 * the battlefield (via {@code chosenPermanentId}). The {@code chosenCreatureId} is
 * {@code null} in the card definition and baked in during effect snapshotting from
 * the source permanent's {@code chosenPermanentId}.
 *
 * <p>Used by cards like Dauntless Bodyguard: "Sacrifice this creature: The chosen
 * creature gains indestructible until end of turn."
 */
public record GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword keyword, UUID chosenCreatureId) implements CardEffect {
}
