package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the players {@code players} names as the effect resolves can't gain life for the rest of the turn,
 * a floating effect on each that lasts until end of turn: by default every player (Skullcrack), else each opponent
 * (Roiling Vortex). Unlike the static {@link PlayersCantGainLifeEffect}, it doesn't rely on a permanent staying on
 * the battlefield.
 *
 * @param players who can't gain life; never a target
 */
public record PlayersCantGainLifeThisTurnEffect(PlayerSubject players) implements CardEffect {

    public PlayersCantGainLifeThisTurnEffect {
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who can't gain life are not targeted: " + players);
        }
    }

    public PlayersCantGainLifeThisTurnEffect() {
        this(Subjects.eachPlayer());
    }
}
