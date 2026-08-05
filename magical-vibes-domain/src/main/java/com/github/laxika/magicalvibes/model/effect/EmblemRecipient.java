package com.github.laxika.magicalvibes.model.effect;

/** Which player a {@link CreateEmblemEffect} gives its emblem to. */
public enum EmblemRecipient {

    /** "You get an emblem with …" — the emblem is created under the ability's controller. */
    CONTROLLER,

    /**
     * "Target opponent gets an emblem with …" — the emblem is created under the targeted player's
     * control, so its static effects apply to that player (Garruk, Apex Predator −8).
     */
    TARGET_PLAYER
}
