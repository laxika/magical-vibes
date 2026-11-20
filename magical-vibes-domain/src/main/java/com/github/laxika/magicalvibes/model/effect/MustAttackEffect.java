package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: creature(s) must attack each combat if able.
 * Per CR 508.1d, the controller is not required to pay any attack costs
 * (e.g. Ghostly Prison tax) even if this effect is present.
 *
 * @param subject determines which creatures are affected. {@code null} means the creature
 *              carrying this effect (SELF). Use {@link Subjects#creaturesEnchantedPlayerControls()}
 *              for curses like Curse of the Nightly Hunt, or
 *              {@link Subjects#creaturesYouControl()} / {@link Subjects#otherCreaturesYouControl()} for
 *              "creatures you control attack each combat if able" (Hellraiser Goblin).
 */
public record MustAttackEffect(PermanentSubject subject) implements CardEffect {

    /** Self-targeting: the creature carrying this effect must attack. */
    public MustAttackEffect() {
        this(null);
    }
}
