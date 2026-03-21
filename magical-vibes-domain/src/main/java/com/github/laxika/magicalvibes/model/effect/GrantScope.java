package com.github.laxika.magicalvibes.model.effect;

public enum GrantScope {
    SELF,
    TARGET,
    ENCHANTED_CREATURE,
    EQUIPPED_CREATURE,
    OWN_TAPPED_CREATURES,
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
    ALL_CREATURES,
    ALL_PERMANENTS,
    ENCHANTED_PLAYER_CREATURES,
    OWN_LANDS
}
