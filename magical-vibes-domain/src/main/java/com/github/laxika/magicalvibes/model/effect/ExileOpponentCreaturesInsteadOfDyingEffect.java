package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a creature an opponent controls would die, exile it instead."
 *
 * <p>Only replaces a creature moving from the battlefield to a graveyard (dying), and only for
 * creatures controlled by an opponent of this permanent's controller. Because the creature never
 * reaches a graveyard, its dies-triggers do not fire. Applied in {@code PermanentRemovalService},
 * which is the one place that knows both the dying permanent's controller and that it was a
 * creature. Used by Liesa, Forgotten Archangel. The parameterized form also models Draugr
 * Necromancer's nontoken-only variant, which adds an ice counter to the exiled card.
 *
 * @param nontokenOnly whether token creatures are excluded
 * @param addIceCounter whether the exiled card receives an ice counter
 */
public record ExileOpponentCreaturesInsteadOfDyingEffect(boolean nontokenOnly, boolean addIceCounter,
                                                         CardEffect whenExiledEffect)
        implements CardEffect {

    public ExileOpponentCreaturesInsteadOfDyingEffect(boolean nontokenOnly, boolean addIceCounter) {
        this(nontokenOnly, addIceCounter, null);
    }

    public ExileOpponentCreaturesInsteadOfDyingEffect(boolean nontokenOnly,
                                                       CardEffect whenExiledEffect) {
        this(nontokenOnly, false, whenExiledEffect);
    }

    public ExileOpponentCreaturesInsteadOfDyingEffect() {
        this(false, false, null);
    }

    public static ExileOpponentCreaturesInsteadOfDyingEffect withIceCounter() {
        return new ExileOpponentCreaturesInsteadOfDyingEffect(true, true);
    }
}
