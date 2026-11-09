package com.github.laxika.magicalvibes.model.effect;

/**
 * Which player a {@link SkipNextEffect} queues its skip on.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the resolving controller ("you skip your next turn"; Chronatog,
 *       Meditate, Ivory Gargoyle). Read from the stack entry's {@code controllerId}; the effect
 *       targets nothing.</li>
 *   <li>{@link #DAMAGED_PLAYER} — the player a combat-damage trigger recorded as the stack entry's
 *       {@code targetId} ("whenever this creature deals combat damage to a player, <em>that
 *       player</em> …"; Blinding Angel). Not a target: the damage-to-a-player collector records the
 *       player dealt damage as the entry's non-targeting {@code targetId}.</li>
 *   <li>{@link #TARGET_PLAYER} — a targeted player, also read from {@code targetId} ("target player
 *       skips …"; False Peace, Empty City Ruse, Stonehorn Dignitary, Yosei, the Morning Star). This
 *       is the only value that declares a player {@code TargetSpec}.</li>
 *   <li>{@link #EACH_OPPONENT} — every player other than the resolving controller ("each opponent
 *       skips …"; Brine Elemental).</li>
 * </ul>
 */
public enum SkipRecipient {
    CONTROLLER,
    DAMAGED_PLAYER,
    TARGET_PLAYER,
    EACH_OPPONENT
}
