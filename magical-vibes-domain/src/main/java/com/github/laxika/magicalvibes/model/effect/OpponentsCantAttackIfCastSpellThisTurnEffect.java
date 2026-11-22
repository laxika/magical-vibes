package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each of the players {@code players} names who cast a spell this turn can't attack with creatures: each
 * opponent by default (Angelic Arbiter), or the controller ({@link Subjects#you()}, Prahv).
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record OpponentsCantAttackIfCastSpellThisTurnEffect(PlayerSubject players) implements CardEffect {

    public OpponentsCantAttackIfCastSpellThisTurnEffect {
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.You)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public OpponentsCantAttackIfCastSpellThisTurnEffect() {
        this(Subjects.eachOpponent());
    }
}
