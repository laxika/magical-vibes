package com.github.laxika.magicalvibes.model.effect;

public enum GrantScope {
    SELF,
    TARGET,
    ENCHANTED_CREATURE,
    ENCHANTED_PERMANENT,
    EQUIPPED_CREATURE,
    OWN_TAPPED_CREATURES,
    OWN_UNTAPPED_CREATURES,
    /**
     * All creatures you control <em>except</em> the source permanent itself.
     * Use this for "other creatures you control" effects, or pair with {@link #SELF}
     * for "creatures you control" when the source is always eligible regardless of filters.
     */
    OWN_CREATURES,
    /**
     * All creatures you control <em>including</em> the source permanent itself.
     * Use this for "creatures you control" effects where the source must also pass
     * the same filter (e.g. "Werewolves you control have menace" — if the source
     * loses the Werewolf subtype, it should also lose the granted keyword).
     */
    ALL_OWN_CREATURES,
    OPPONENT_CREATURES,
    OWN_PERMANENTS,
    /** All creatures the targeted player controls (one-shot, e.g. Shields of Velis Vel). */
    TARGET_PLAYERS_CREATURES,
    ALL_CREATURES,
    ALL_PERMANENTS,
    ENCHANTED_PLAYER_CREATURES,
    OWN_LANDS,
    /** All lands on the battlefield, regardless of controller (Natural Affinity). */
    ALL_LANDS,
    /**
     * The tokens created by earlier effects in this same resolution (read from
     * {@code StackEntry.createdPermanentIds}). Use for "those tokens gain [keyword]" clauses that
     * follow a token-creation effect on the same spell/ability, e.g. Gilt-Leaf Ambush's clash-win
     * deathtouch grant.
     */
    TOKENS_CREATED_THIS_RESOLUTION
}
