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
 */
public record ExileTargetPlayerHandEffect(PlayerSubject players, boolean trackWithSource) implements CardEffect {

    public ExileTargetPlayerHandEffect {
        if (players == null) {
            throw new IllegalArgumentException("ExileTargetPlayerHandEffect requires a player subject");
        }
    }

    public ExileTargetPlayerHandEffect(boolean trackWithSource) {
        this(Subjects.targetPlayer(), trackWithSource);
    }

    public ExileTargetPlayerHandEffect() {
        this(false);
    }

    /** "Exile all cards from [players'] hands", untracked ("all hands": {@code eachPlayer()}). */
    public static ExileTargetPlayerHandEffect handsOf(PlayerSubject players) {
        return new ExileTargetPlayerHandEffect(players, false);
    }

    /** "Exile all cards from [players'] hands", tracked with the source permanent ("exiled with this creature"). */
    public static ExileTargetPlayerHandEffect handsOfWithSource(PlayerSubject players) {
        return new ExileTargetPlayerHandEffect(players, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return players.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
