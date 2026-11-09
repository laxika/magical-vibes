package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell cast by the player an ability's registration watches: the "they" of "until the end of defending
 * player's next turn, that player gets two rad counters whenever they cast a spell" (Nuka-Nuke Launcher). Only the
 * collector of the abilities a resolving ability registered reads it, against the player the registration recorded
 * ({@code TemporaryGlobalTriggeredAbility.watchedPlayerId}); any other reader matches no spell.
 */
public record StackEntryControlledByWatchedPlayerPredicate() implements StackEntryPredicate {
}
