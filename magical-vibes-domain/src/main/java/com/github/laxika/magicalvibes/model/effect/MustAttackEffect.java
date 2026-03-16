package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: creature(s) must attack each combat if able.
 * Per CR 508.1d, the controller is not required to pay any attack costs
 * (e.g. Ghostly Prison tax) even if this effect is present.
 *
 * @param scope determines which creatures are affected. {@code null} means the creature
 *              carrying this effect (SELF). Use {@link GrantScope#ENCHANTED_PLAYER_CREATURES}
 *              for curses like Curse of the Nightly Hunt.
 */
public record MustAttackEffect(GrantScope scope) implements CardEffect {

    /** Self-targeting: the creature carrying this effect must attack. */
    public MustAttackEffect() {
        this(null);
    }
}
