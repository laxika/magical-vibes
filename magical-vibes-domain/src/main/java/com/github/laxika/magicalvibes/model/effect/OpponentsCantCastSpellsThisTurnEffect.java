package com.github.laxika.magicalvibes.model.effect;

/**
 * The players {@code players} names as the effect resolves can't cast spells this turn: by default each opponent of
 * the controller (Silence), else the defending player (Xantid Swarm) or every player (Bilbo's Gambit).
 *
 * @param players who can't cast; never a target
 */
public record OpponentsCantCastSpellsThisTurnEffect(PlayerSubject players) implements CardEffect {

    public OpponentsCantCastSpellsThisTurnEffect {
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who can't cast are not targeted: " + players);
        }
    }

    public OpponentsCantCastSpellsThisTurnEffect() {
        this(Subjects.eachOpponent());
    }
}
