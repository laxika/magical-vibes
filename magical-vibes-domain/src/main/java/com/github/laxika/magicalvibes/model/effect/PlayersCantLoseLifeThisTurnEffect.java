package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the players {@code players} names as the effect resolves can't lose life for the rest of the turn:
 * every player by default (Everybody Lives!), or the controller ({@link Subjects#you()}, Courageous Resolve).
 *
 * @param players who can't lose life; never a target
 */
public record PlayersCantLoseLifeThisTurnEffect(PlayerSubject players) implements CardEffect {

    public PlayersCantLoseLifeThisTurnEffect {
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who can't lose life are not targeted: " + players);
        }
    }

    public PlayersCantLoseLifeThisTurnEffect() {
        this(Subjects.eachPlayer());
    }
}
