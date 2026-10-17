package com.github.laxika.magicalvibes.model.effect;

/**
 * Misfortune. An opponent chooses one — either you put a +1/+1 counter on each creature you control
 * and gain 4 life, or you put a -1/-1 counter on each creature that player controls and Misfortune
 * deals 4 damage to that player. Per CR 700.2e the opponent chooses the mode while the spell is cast,
 * through the may-ability (accept/decline) system; the choice is recorded on the spell as
 * {@code accepted} (accept = counters and life, decline = shrink and burn) and carried out on resolution.
 *
 * @param accepted the mode the opponent chose, or {@code null} while no choice has been made
 */
public record MisfortuneEffect(Boolean accepted) implements CardEffect {

    public MisfortuneEffect() {
        this(null);
    }
}
