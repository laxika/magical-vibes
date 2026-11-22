package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each of the players {@code players} names who attacked with a creature this turn can't cast spells:
 * each opponent by default (Angelic Arbiter; with the scoped form only those who attacked the controller or their
 * planeswalker, Sandswirl Wanderglyph), or the controller ({@link Subjects#you()}, Prahv).
 *
 * @param onlyIfAttackedControllerOrPlaneswalker whether only an attack on the controller or their planeswalker counts
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record OpponentsCantCastSpellsIfAttackedThisTurnEffect(boolean onlyIfAttackedControllerOrPlaneswalker, PlayerSubject players) implements CardEffect {

    public OpponentsCantCastSpellsIfAttackedThisTurnEffect {
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.You)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public OpponentsCantCastSpellsIfAttackedThisTurnEffect(boolean onlyIfAttackedControllerOrPlaneswalker) {
        this(onlyIfAttackedControllerOrPlaneswalker, Subjects.eachOpponent());
    }

    public OpponentsCantCastSpellsIfAttackedThisTurnEffect() {
        this(false);
    }

    public OpponentsCantCastSpellsIfAttackedThisTurnEffect(PlayerSubject players) {
        this(false, players);
    }
}
