package com.github.laxika.magicalvibes.model.effect;

public enum GrantScope {
    SELF,
    /** The permanent referenced by the triggering event of a triggered ability. */
    TRIGGERING_PERMANENT,
    /**
     * The source permanent and the creature it is soulbond-paired with (CR 702.94).
     * Used for "as long as ~ is paired with another creature, each of those creatures has …".
     */
    SELF_AND_PAIRED,
    TARGET,
    /** The target creature and every other creature sharing a color with it. */
    TARGET_AND_SHARING_CREATURES,
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
    /** All lands the targeted player controls (one-shot animation effects). */
    TARGET_PLAYERS_LANDS,
    /**
     * All creatures on the battlefield <em>except</em> the source permanent itself.
     * Use this for "other …" wordings, or when the source can never match the filter.
     */
    ALL_CREATURES,
    /**
     * All creatures on the battlefield <em>including</em> the source permanent itself.
     * Use this for global anthems whose filter the source also passes
     * ("Minotaur creatures get +1/+0" on a Minotaur) — if the source loses the
     * subtype it stops boosting itself as well.
     */
    ALL_CREATURES_INCLUDING_SELF,
    /**
     * All permanents on the battlefield <em>except</em> the source permanent itself, regardless of
     * controller. There is no self-including counterpart: pair this with {@link #SELF} when the
     * source must be affected too.
     */
    ALL_PERMANENTS,
    ENCHANTED_PLAYER_CREATURES,
    OWN_LANDS,
    OPPONENT_LANDS,
    /** All lands on the battlefield, regardless of controller (Natural Affinity). */
    ALL_LANDS,
    /**
     * All lands on the battlefield <em>including</em> the source permanent itself, regardless of
     * controller. Use this for "each land is …" wordings whose source is a land too and therefore
     * affects itself (Urborg, Tomb of Yawgmoth).
     */
    ALL_LANDS_INCLUDING_SELF,
    /**
     * The tokens created by earlier effects in this same resolution (read from
     * {@code StackEntry.createdPermanentIds}). Use for "those tokens gain [keyword]" clauses that
     * follow a token-creation effect on the same spell/ability, e.g. Gilt-Leaf Ambush's clash-win
     * deathtouch grant.
     */
    TOKENS_CREATED_THIS_RESOLUTION,
    /**
     * The other attacking creatures in the same attacking band (CR 702.22) as the source
     * permanent. The source itself is excluded — "banded with this creature" means the rest
     * of the band. Yields nothing when the source is not attacking in a band.
     */
    BANDED_WITH_SELF
}
