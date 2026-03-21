package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, grants the controller the ability to copy every instant or sorcery
 * spell they cast for the rest of the turn. Each copy may have new targets chosen.
 *
 * <p>Used by The Mirari Conjecture chapter III. At resolution time, the controller's
 * player ID is added to {@code GameData.playersWithSpellCopyUntilEndOfTurn}; the
 * flag is automatically cleared during end-of-turn cleanup.</p>
 */
public record GrantInstantSorceryCopyUntilEndOfTurnEffect() implements CardEffect {
}
