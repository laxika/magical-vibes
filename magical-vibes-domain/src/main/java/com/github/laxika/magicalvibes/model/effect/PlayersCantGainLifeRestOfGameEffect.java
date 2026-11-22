package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the player {@code player} names as the effect resolves can't gain life for the rest of the game:
 * the controller by default (Welcome the Darkness), or "that player" of the trigger event
 * ({@link Subjects#triggeringPlayer()}, Stigma Lasher). The resolution registers it as a floating effect on that player.
 *
 * @param player who can't gain life; never a target
 */
public record PlayersCantGainLifeRestOfGameEffect(PlayerSubject player) implements CardEffect {

    public PlayersCantGainLifeRestOfGameEffect {
        if (player.declaresTarget() || player instanceof PlayerSubject.Each) {
            throw new IllegalArgumentException("One untargeted player can't gain life: " + player);
        }
    }

    /** "You can't gain life for the rest of the game." */
    public PlayersCantGainLifeRestOfGameEffect() {
        this(Subjects.you());
    }
}
