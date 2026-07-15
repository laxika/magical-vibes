package com.github.laxika.magicalvibes.model.effect;

/**
 * The kind of target an effect legally accepts, in one declarative value.
 *
 * <p>This enum is the successor to the eleven per-effect {@code canTarget*} / targeting
 * booleans on {@link CardEffect}: an effect declares its targeting once through a
 * {@link TargetSpec} carrying one {@code TargetCategory}, and the legacy booleans are derived
 * from it (see the {@code CardEffect} defaults). Fine-grained narrowing (artifact-only,
 * nonland, a subtype, …) is expressed with the spec's {@link TargetSpec#predicate()}, NOT with
 * a new category.</p>
 *
 * <p>{@link #includesPermanents()} and {@link #includesPlayers()} exist so the legacy
 * {@code canTargetPermanent} / {@code canTargetPlayer} defaults can be derived from the
 * category. They mirror exactly what those booleans meant before migration: e.g. a
 * {@code CREATURE} (or any battlefield category) includes permanents; a bare {@code PLAYER}
 * includes only players; {@code ANY_TARGET} includes both; zone categories
 * ({@code SPELL_ON_STACK}, graveyard / exile) and {@code NONE} include neither (those zones
 * are guarded by their own paths).</p>
 */
public enum TargetCategory {
    /** The effect targets nothing (self-targeting / metadata-only / untargeted). */
    NONE(false, false),
    /** A player. */
    PLAYER(false, true),
    /** A player or a permanent (the generic "any permanent or player" split). */
    PLAYER_OR_PERMANENT(true, true),
    /** Any permanent on the battlefield (the card/ability filter narrows further). */
    PERMANENT(true, false),
    /** A creature (layer-aware creature check). */
    CREATURE(true, false),
    /** A land. */
    LAND(true, false),
    /** A creature or a planeswalker. */
    CREATURE_OR_PLANESWALKER(true, false),
    /** A player or a planeswalker. */
    PLAYER_OR_PLANESWALKER(true, true),
    /** Any target: a creature, a planeswalker, or a player (burn). */
    ANY_TARGET(true, true),
    /** A spell on the stack (counter / redirect). */
    SPELL_ON_STACK(false, false),
    /**
     * A card in a graveyard restricted to an opponent's graveyard — the default graveyard state
     * ({@code canTargetGraveyard=true}, {@code canTargetAnyGraveyard=false},
     * {@code targetsControllersGraveyardOnly=false}). "Opponent's graveyard only" because the
     * graveyard readers (SpellCastingService / AiTargetSelector) treat a non-any, non-controllers
     * graveyard effect as opponent-scoped; effects that actually allow the controller's own
     * graveyard enforce that through their kept validator, not through these booleans.
     */
    GRAVEYARD_CARD(false, false),
    /** A card in any player's graveyard ({@code canTargetAnyGraveyard=true}). */
    ANY_GRAVEYARD_CARD(false, false),
    /**
     * A card restricted to the controller's own graveyard
     * ({@code targetsControllersGraveyardOnly=true}). The dedicated correlate of the orthogonal
     * {@code targetsControllersGraveyardOnly} boolean: the three graveyard zone-states
     * (opponent-only / any / controller-only) form one mutually-exclusive dimension, so each is one
     * category. See {@code GrantFlashbackToTargetGraveyardCardEffect},
     * {@code PlayTargetCardFromGraveyardWithoutPayingManaCostEffect}.
     */
    CONTROLLERS_GRAVEYARD_CARD(false, false),
    /** A card in exile. */
    EXILE_CARD(false, false);

    private final boolean includesPermanents;
    private final boolean includesPlayers;

    TargetCategory(boolean includesPermanents, boolean includesPlayers) {
        this.includesPermanents = includesPermanents;
        this.includesPlayers = includesPlayers;
    }

    /** Whether a permanent on the battlefield is a legal target for this category. */
    public boolean includesPermanents() {
        return includesPermanents;
    }

    /** Whether a player is a legal target for this category. */
    public boolean includesPlayers() {
        return includesPlayers;
    }
}
