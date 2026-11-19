package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves the top card of each named player's library into the ante zone (several players in APNAP order).
 *
 * @param player whose top card is anted; declares no target
 */
public record AnteTopCardEffect(PlayerSubject player) implements CardEffect {

    /** Moves the controller's top library card into the ante zone. */
    public AnteTopCardEffect() {
        this(Subjects.you());
    }

    public AnteTopCardEffect {
        if (player.declaresTarget()) {
            throw new IllegalArgumentException("An ante effect declares no target: " + player);
        }
    }
}
