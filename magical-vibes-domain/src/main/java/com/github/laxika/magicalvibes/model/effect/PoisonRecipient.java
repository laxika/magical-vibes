package com.github.laxika.magicalvibes.model.effect;

/**
 * Who gets poison counters when a {@link GivePoisonCountersEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller (e.g. Phyrexian Vatmother's upkeep trigger
 *       poisons its own controller).</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}); the effect
 *       targets a player. Doubles as an {@code ON_CONTROLLER_CASTS_SPELL} trigger descriptor when a
 *       {@code spellFilter} is present (Hand of the Praetors).</li>
 *   <li>{@link #EACH_PLAYER} — every player gets poison, including the controller (Ichor Rats).</li>
 *   <li>{@link #ENCHANTED_PERMANENT_CONTROLLER} — the controller of the enchanted permanent, whose
 *       id is baked into {@code affectedPlayerId} at trigger time (Relic Putrescence).</li>
 * </ul>
 */
public enum PoisonRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    EACH_PLAYER,
    ENCHANTED_PERMANENT_CONTROLLER
}
