package com.github.laxika.magicalvibes.model.effect;

/**
 * Whose libraries an {@link ExileTopCardsToSourceEffect} exiles from.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — only the effect's controller exiles, read from the stack entry's
 *       {@code controllerId} (Colfenor's Plans, Duplicity, Search the City).</li>
 *   <li>{@link #TARGET_PLAYER} — the chosen player exiles. Read from the stack entry's
 *       {@code targetId} (Mindreaver).</li>
 *   <li>{@link #TARGET_OPPONENT} — a single opponent exiles. Read from the stack entry's
 *       {@code targetId} when a combat-damage trigger bound the damaged player (Nightveil
 *       Specter), or from {@code attackedTargetId} for an attack trigger (Robber of the Rich);
 *       otherwise the sole opponent in a two-player game (Grimoire Thief).</li>
 *   <li>{@link #EACH_PLAYER} — every player exiles, in {@code orderedPlayerIds} order
 *       (Knowledge Pool).</li>
 * </ul>
 *
 * <p>Distinct from {@link LibraryOwner}, which is about <em>inspecting</em> a library and whose
 * {@code TARGET_PLAYER} is a genuine chosen player target. The ordinary
 * {@code TARGET_OPPONENT} use here is derived from the trigger context and declares no
 * {@code TargetSpec}; {@link ExileTopCardsToSourceEffect#targetedOpponent()} enables the chosen
 * opponent variant used by targeted activated abilities.
 */
public enum LibraryScope {
    CONTROLLER,
    TARGET_PLAYER,
    TARGET_OPPONENT,
    EACH_PLAYER
}
