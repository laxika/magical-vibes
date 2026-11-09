package com.github.laxika.magicalvibes.model.effect;

/**
 * Who loses life when a {@link LoseLifeEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller loses life (a cost/drawback, e.g. Phyrexian
 *       Rager).</li>
 *   <li>{@link #OWNER} — the owner of the effect's source card loses life, even after a control
 *       change.</li>
 *   <li>{@link #SOURCE_CONTROLLER} — the current controller of the effect's source permanent loses
 *       life, or its last known controller if it has left the battlefield.</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) loses life;
 *       the effect targets a player.</li>
 *   <li>{@link #TRIGGERING_PLAYER} — the player whose spell or event caused the trigger
 *       (stack entry's {@code targetId}) loses life; the effect does not target that player.</li>
 *   <li>{@link #TARGET_PERMANENT_CONTROLLER} — the controller of the targeted permanent (stack
 *       entry's {@code targetId} is a permanent, not a player) loses life; the effect does not add
 *       its own target. Used by "destroy target creature; ... its controller loses N life"
 *       (Soul Reap). Resolve before any accompanying destroy so the permanent is still present.</li>
 *   <li>{@link #DYING_CREATURE_CONTROLLER} — the last-known controller of the creature that died,
 *       for {@code Triggers.creatureDies(DAMAGED_BY_THIS)} triggers such as "whenever a creature dealt damage by
 *       that creature dies this turn, its controller loses 2 life" (Touch of Moonglove). The
 *       death collectors record that player on every entry as its {@code triggeringPermanentControllerId},
 *       whatever the effects are; the effect chooses no target.</li>
 *   <li>{@link #TRIGGERING_PERMANENT_CONTROLLER} — the controller of the permanent the trigger event was about
 *       ({@code StackEntry.triggeringPermanentId}) as the ability resolves, or its last known controller once it has
 *       left the battlefield, or the controller the entry recorded for a source that was never a permanent: "its
 *       controller loses that much life" of an enchanted creature that dealt damage (Visions of Brutality). The
 *       effect chooses no target.</li>
 *   <li>{@link #TRIGGERING_CARD_OWNER} — the owner of the card a "whenever you play a card" event was about (the
 *       triggering spell's card, or the land played): "that card's owner loses 3 life" (The Matrix of Time). The
 *       effect chooses no target.</li>
 *   <li>{@link #DEFENDING_PLAYER} — the player being attacked by the source (or the controller of
 *       the attacked planeswalker), captured as the trigger's {@code attackedTargetId}; used by
 *       combat triggers such as "whenever this creature becomes blocked, defending player loses N
 *       life" (Vedalken Ghoul). The effect chooses no target.</li>
 *   <li>{@link #OTHER_CHOSEN_PLAYER} — each player the source permanent chose as it entered other than the
 *       player the trigger event was about ({@code StackEntry.triggeringPlayerId}): "the other chosen player" of Sower
 *       of Discord. The chosen players are read from the source as the ability resolves, or from its last-known
 *       information once it has left the battlefield. The effect chooses no target.</li>
 *   <li>{@link #EACH_PLAYER} — every player loses life, in {@code orderedPlayerIds} order.</li>
 *   <li>{@link #EACH_OPPONENT} — every opponent of the controller loses life, in
 *       {@code orderedPlayerIds} order.</li>
 *   <li>{@link #EACH_OTHER_PLAYER} — every player other than the source card's owner loses life,
 *       in {@code orderedPlayerIds} order.</li>
 * </ul>
 */
public enum LoseLifeRecipient {
    CONTROLLER,
    OWNER,
    SOURCE_CONTROLLER,
    TARGET_PLAYER,
    TRIGGERING_PLAYER,
    TARGET_PERMANENT_CONTROLLER,
    DYING_CREATURE_CONTROLLER,
    TRIGGERING_PERMANENT_CONTROLLER,
    TRIGGERING_CARD_OWNER,
    DEFENDING_PLAYER,
    OTHER_CHOSEN_PLAYER,
    /** The player whose upkeep is currently resolving; supplied by the trigger entry. */
    ACTIVE_PLAYER,
    EACH_PLAYER,
    EACH_OPPONENT,
    EACH_OTHER_PLAYER
}
