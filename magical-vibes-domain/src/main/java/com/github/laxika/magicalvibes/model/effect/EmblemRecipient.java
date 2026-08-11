package com.github.laxika.magicalvibes.model.effect;

/** Which player a {@link CreateEmblemEffect} gives its emblem to. */
public enum EmblemRecipient {

    /** "You get an emblem with …" — the emblem is created under the ability's controller. */
    CONTROLLER,

    /**
     * "Target opponent gets an emblem with …" — the emblem is created under the targeted player's
     * control, so its static effects apply to that player (Garruk, Apex Predator −8).
     */
    TARGET_PLAYER,

    /** "Each opponent gets an emblem with …" — one emblem under every opponent's control. */
    EACH_OPPONENT,

    /**
     * "Each player dealt damage this way gets an emblem with …" — one emblem per player this same
     * stack entry actually dealt damage to (Chandra, Roaring Flame −7). A player whose damage was
     * fully prevented gets no emblem, so this reads the resolution's damage log rather than
     * recomputing who was in scope.
     */
    EACH_PLAYER_DEALT_DAMAGE_THIS_WAY
}
