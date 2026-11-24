package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Players play with their hands revealed" (Zur's Weirding, Wandering Eye, Revelation; {@code eachPlayer()}), "play
 * with your hand revealed" (Enduring Renewal; {@code you()}): each hand {@code players} names is visible to every
 * player. As a static ability its players are judged against the source and its controller; floated on a player by
 * {@link GrantStaticEffectToPlayerEffect} ("that player plays with their hand revealed for the rest of the game",
 * Truth or Dare; "for as long as this creature remains on the battlefield", Stromgald Spy) it reveals the hand of the
 * player it is on. Read by {@code GameViewProjectionFactory.getRevealedOpponentHand}.
 *
 * @param players whose hands are revealed
 */
public record PlayWithHandsRevealedEffect(PlayerSubject players) implements CardEffect {

    public PlayWithHandsRevealedEffect() {
        this(Subjects.eachPlayer());
    }

    public PlayWithHandsRevealedEffect {
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("A static hand reveal takes no target player");
        }
    }
}
