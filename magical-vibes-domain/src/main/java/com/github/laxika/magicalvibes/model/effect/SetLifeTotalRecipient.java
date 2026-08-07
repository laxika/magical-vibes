package com.github.laxika.magicalvibes.model.effect;

/**
 * Whose life total a {@link SetLifeTotalEffect} sets.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller ("your life total becomes N"; Form of the
 *       Dragon, Invincible Hymn). The amount is evaluated from the controller's point of view.</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}); the effect
 *       targets a player (Magister Sphinx, Sorin Markov, Torgaar).</li>
 *   <li>{@link #EACH_PLAYER} — every player, in {@code orderedPlayerIds} order ("each player's life
 *       total becomes …"; Worldfire, Biorhythm, Arbiter of Knollridge). The amount is evaluated once
 *       <em>per player</em>, with that player standing in as the amount's controller, so a
 *       {@code CountScope.CONTROLLER} amount reads "the number of creatures <em>they</em> control".
 *       Every new total is determined before any of them is applied, so one player's change can
 *       never feed back into another player's amount.</li>
 * </ul>
 */
public enum SetLifeTotalRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    EACH_PLAYER
}
