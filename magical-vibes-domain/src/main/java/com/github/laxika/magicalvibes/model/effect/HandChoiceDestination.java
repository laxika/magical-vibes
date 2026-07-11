package com.github.laxika.magicalvibes.model.effect;

/**
 * Where the caster-chosen card goes when a {@link ChooseCardsFromTargetHandEffect} resolves.
 *
 * <ul>
 *   <li>{@link #DISCARD} — the chosen card is discarded (fires discard triggers and sets
 *       {@code discardCausedByOpponent}; e.g. Duress, Distress).</li>
 *   <li>{@link #EXILE} — the chosen card is exiled; with {@code returnOnSourceLeave} it returns
 *       to hand when the source permanent leaves (e.g. Kitesail Freebooter, Night Terrors).</li>
 *   <li>{@link #TOP_OF_LIBRARY} — the chosen card(s) are put on top of the target's library,
 *       first chosen ending up on top (e.g. Agonizing Memories).</li>
 * </ul>
 */
public enum HandChoiceDestination {
    DISCARD,
    EXILE,
    TOP_OF_LIBRARY
}
