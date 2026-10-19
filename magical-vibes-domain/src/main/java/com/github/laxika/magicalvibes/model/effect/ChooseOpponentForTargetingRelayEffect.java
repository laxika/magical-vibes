package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses an opponent and remembers that player on the source permanent.
 *
 * @param secret when true the choice is a secret choice the source's controller can later reveal as a cost
 *               ("When this creature enters, secretly choose an opponent" — Stalking Leonin), so it is also
 *               recorded as the permanent's chosen player
 */
public record ChooseOpponentForTargetingRelayEffect(boolean secret) implements CardEffect {

    public ChooseOpponentForTargetingRelayEffect() {
        this(false);
    }
}
