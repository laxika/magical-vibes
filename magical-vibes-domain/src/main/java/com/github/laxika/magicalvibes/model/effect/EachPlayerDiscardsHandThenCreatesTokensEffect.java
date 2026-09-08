package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards their entire hand, then creates one copy of {@code token} for each card
 * they discarded. The token count is independent for each player.
 */
public record EachPlayerDiscardsHandThenCreatesTokensEffect(CreateTokenEffect token) implements CardEffect {

    /** Awaken the Erstwhile's 2/2 black Zombie token template. */
    public EachPlayerDiscardsHandThenCreatesTokensEffect() {
        this(CreateTokenEffect.blackZombie(1));
    }
}
