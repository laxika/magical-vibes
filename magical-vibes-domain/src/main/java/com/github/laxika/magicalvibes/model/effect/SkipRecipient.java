package com.github.laxika.magicalvibes.model.effect;

/**
 * Which player a {@link SkipNextEffect} queues its skip on.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the resolving controller ("you skip your next turn"; Chronatog,
 *       Meditate, Ivory Gargoyle). Read from the stack entry's {@code controllerId}; the effect
 *       targets nothing.</li>
 *   <li>{@link #DAMAGED_PLAYER} — the player a combat-damage trigger baked into the stack entry's
 *       {@code targetId} ("whenever this creature deals combat damage to a player, <em>that
 *       player</em> …"; Blinding Angel). Not a target: the effect asks the combat engine for the
 *       {@link CombatDamageTriggerContextEffect.TriggerContext#DAMAGED_PLAYER} stack-entry shape
 *       instead of declaring a {@code TargetSpec}.</li>
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
