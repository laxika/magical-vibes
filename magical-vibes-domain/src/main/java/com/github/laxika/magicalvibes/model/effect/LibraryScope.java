package com.github.laxika.magicalvibes.model.effect;

/**
 * Whose libraries an {@link ExileTopCardsToSourceEffect} exiles from.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — only the effect's controller exiles, read from the stack entry's
 *       {@code controllerId} (Colfenor's Plans, Duplicity, Search the City).</li>
 *   <li>{@link #TARGET_OPPONENT} — a single opponent exiles. Read from the stack entry's
 *       {@code targetId} when a combat-damage trigger bound the damaged player (Nightveil
 *       Specter), otherwise the sole opponent in a two-player game (Grimoire Thief).</li>
 *   <li>{@link #EACH_PLAYER} — every player exiles, in {@code orderedPlayerIds} order
 *       (Knowledge Pool).</li>
 * </ul>
 *
 * <p>Distinct from {@link LibraryOwner}, which is about <em>inspecting</em> a library and whose
 * {@code TARGET_PLAYER} is a genuine chosen player target. {@code TARGET_OPPONENT} here is never
 * chosen by the controller — it is derived from the trigger context — so it declares no
 * {@code TargetSpec}.
 */
public enum LibraryScope {
    CONTROLLER,
    TARGET_OPPONENT,
    EACH_PLAYER
}
