package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile all cards from target player's hand", "exile all cards from all hands", "exile all cards from all opponents'
 * hands", "exile all cards from your hand": every card in the hand of each player {@code players} names is exiled face
 * up; there is no player choice and no play permission. Exiling from hand is not a discard, so no discard triggers
 * fire. When {@code trackWithSource} is true, the cards are also associated with the source permanent for later
 * effects that refer to cards exiled with it (Hypnox, Wormfang Behemoth, Ashiok, Nightmare Weaver). Identity Crisis
 * pairs it with an {@link ExileGraveyardCardsEffect} ({@code TARGET_PLAYER_ENTIRE}) on the same target player.
 *
 * @param players         whose hands are exiled; only {@code targetPlayer()} declares the (harmful) player target
 * @param trackWithSource whether the exiled cards are tracked with the source permanent
 *
 * <p>The step records its {@code StepKind.EXILE} ("that many", "those cards").
 *
 * @param faceDown whether the cards are exiled face down (CR 406.3)
 */
public record ExileTargetPlayerHandEffect(PlayerSubject players, boolean trackWithSource, boolean faceDown)
        implements CardEffect {

    public ExileTargetPlayerHandEffect {
        if (players == null) {
            throw new IllegalArgumentException("ExileTargetPlayerHandEffect requires a player subject");
        }
    }

    public ExileTargetPlayerHandEffect(boolean trackWithSource) {
        this(Subjects.targetPlayer(), trackWithSource, false);
    }

    public ExileTargetPlayerHandEffect() {
        this(false);
    }

    /** "Exile all cards from [players'] hands", untracked ("all hands": {@code eachPlayer()}). */
    public static ExileTargetPlayerHandEffect handsOf(PlayerSubject players) {
        return new ExileTargetPlayerHandEffect(players, false, false);
    }

    /** "Exile all cards from [players'] hands", tracked with the source permanent ("exiled with this creature"). */
    public static ExileTargetPlayerHandEffect handsOfWithSource(PlayerSubject players) {
        return new ExileTargetPlayerHandEffect(players, true, false);
    }

    /** These hands exiled face down. */
    public ExileTargetPlayerHandEffect exiledFaceDown() {
        return new ExileTargetPlayerHandEffect(players, trackWithSource, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return players.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
