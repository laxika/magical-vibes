package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the players {@code players} names can cast spells only during their own turns: every player by default
 * (Dosan the Falling Leaf), or the controller ({@link Subjects#you()}, Fires of Invention). Unlike
 * {@link PlayersCanCastAndActivateOnlyDuringOwnTurnEffect} this leaves activated abilities untouched.
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record PlayersCanCastSpellsOnlyDuringOwnTurnEffect(PlayerSubject players) implements CardEffect {

    public PlayersCanCastSpellsOnlyDuringOwnTurnEffect {
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.You)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public PlayersCanCastSpellsOnlyDuringOwnTurnEffect() {
        this(Subjects.eachPlayer());
    }
}
