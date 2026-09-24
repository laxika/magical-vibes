package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for a spell's "when this spell is countered or fizzles, you may copy it"
 * ability. The trigger collector snapshots the failed spell and reuses the standard spell-copy
 * effect when the ability resolves.
 */
public record CopyThisSpellWhenCounteredOrFizzlesEffect() implements CardEffect {}
