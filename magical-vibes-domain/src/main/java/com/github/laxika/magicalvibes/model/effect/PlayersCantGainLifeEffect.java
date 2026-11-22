package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the players {@code players} names can't gain life: every player by default, the opponents of this
 * permanent's controller ({@link Subjects#eachOpponent()}, Erebos, God of the Dead) or the player it enchants
 * ({@link Subjects#enchantedPlayer()}, Grievous Wound). Life loss and damage still apply normally; only life gain is
 * prevented.
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record PlayersCantGainLifeEffect(PlayerSubject players) implements CardEffect {

    public PlayersCantGainLifeEffect {
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.EnchantedPlayer)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public PlayersCantGainLifeEffect() {
        this(Subjects.eachPlayer());
    }
}
