package com.github.laxika.magicalvibes.model.effect;

/**
 * Whose library a library-inspection effect acts on. Used by
 * {@link ReorderTopCardsOfLibraryEffect} and {@link RevealTopCardOfLibraryEffect}.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — "your library". Read from the stack entry's {@code controllerId}; the
 *       effect declares no {@code TargetSpec}, so a card carrying it may still target something
 *       else (Discombobulate counters target spell, then looks at its controller's own library).</li>
 *   <li>{@link #TARGET_PLAYER} — "target player's library". Read from the stack entry's
 *       {@code targetId}, and the only value that declares a player {@code TargetSpec}. The chosen
 *       player may be the controller themselves ("target player" includes you).</li>
 *   <li>{@link #OPPONENT} — the resolving controller's opponent's library. The opponent is
 *       derived at resolution and is not a target.</li>
 *   <li>{@link #ENCHANTED_PERMANENT_CONTROLLER} â€” the library of the player baked into the
 *       stack entry by an enchanted-permanent-controller trigger. Read from {@code targetId}, but
 *       does not declare an additional target because the trigger already identifies that player.</li>
 * </ul>
 *
 * <p>Who <em>decides</em> is not this axis: the effect's controller always makes the choice (looks
 * at the cards, picks the order), whichever library is being inspected.
 */
public enum LibraryOwner {
    CONTROLLER,
    TARGET_PLAYER,
    OPPONENT,
    ENCHANTED_PERMANENT_CONTROLLER
}
