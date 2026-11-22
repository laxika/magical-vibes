package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: during combat, the players {@code players} names can't cast spells: the opponents of this permanent's
 * controller by default, or every player ({@link Subjects#eachPlayer()}, Basandra, Battle Seraph).
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record OpponentsCantCastSpellsDuringCombatEffect(PlayerSubject players) implements CardEffect {

    public OpponentsCantCastSpellsDuringCombatEffect {
        if (!(players instanceof PlayerSubject.Each)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public OpponentsCantCastSpellsDuringCombatEffect() {
        this(Subjects.eachOpponent());
    }
}
