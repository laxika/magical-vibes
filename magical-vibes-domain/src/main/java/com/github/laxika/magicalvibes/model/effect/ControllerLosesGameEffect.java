package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "You lose the game" — {@code player} loses the game; the no-argument form is {@link Subjects#you()}, the
 * controller of the resolving ability. {@link Subjects#enchantedPlayer()} is "enchanted player loses the game"
 * (Sinner's Judgment).
 *
 * <p>Non-targeting. Unlike {@link TargetPlayerLosesGameEffect} (which targets or is bound to a specific player at
 * construction), this resolves against the subject's player as the effect resolves, so it can be used by
 * state-triggered / dynamic-controller abilities (e.g. Immortal Coil's empty-graveyard loss).
 */
public record ControllerLosesGameEffect(PlayerSubject player) implements CardEffect {

    public ControllerLosesGameEffect {
        Objects.requireNonNull(player, "player");
        if (player.declaresTarget()) {
            throw new IllegalArgumentException("A target player is TargetPlayerLosesGameEffect's: " + player);
        }
    }

    public ControllerLosesGameEffect() {
        this(Subjects.you());
    }
}
