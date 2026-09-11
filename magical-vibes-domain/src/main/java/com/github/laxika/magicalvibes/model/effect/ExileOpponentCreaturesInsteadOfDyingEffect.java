package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a creature an opponent controls would die, exile it instead."
 *
 * <p>Only replaces a creature moving from the battlefield to a graveyard (dying), and only for
 * creatures controlled by an opponent of this permanent's controller. Because the creature never
 * reaches a graveyard, its dies-triggers do not fire. Applied in {@code PermanentRemovalService},
 * which is the one place that knows both the dying permanent's controller and that it was a
 * creature. Used by Liesa, Forgotten Archangel. The parameterized form also models Draugr
 * Necromancer's nontoken-only variant, Valentin's optional exile rider, and Head of the Hunt's
 * mandatory reflexive token trigger.
 *
 * @param nontokenOnly whether token creatures are excluded
 * @param addIceCounter whether the exiled card receives an ice counter
 * @param whenExiledEffect an optional effect queued after a creature is exiled; may effects keep
 *        their choice wrapper, while other effects are queued as mandatory triggered abilities
 * @param trackWithSource whether the exiled card is tracked with the permanent carrying this effect
 * @param lifeGainOnExile life gained by the replacement effect's controller for each exile
 */
public record ExileOpponentCreaturesInsteadOfDyingEffect(boolean nontokenOnly, boolean addIceCounter,
                                                         CardEffect whenExiledEffect,
                                                         boolean trackWithSource,
                                                         int lifeGainOnExile)
        implements CardEffect {

    public ExileOpponentCreaturesInsteadOfDyingEffect(boolean nontokenOnly, boolean addIceCounter) {
        this(nontokenOnly, addIceCounter, null, false, 0);
    }

    public ExileOpponentCreaturesInsteadOfDyingEffect(boolean nontokenOnly,
                                                       CardEffect whenExiledEffect) {
        this(nontokenOnly, false, whenExiledEffect, false, 0);
    }

    public ExileOpponentCreaturesInsteadOfDyingEffect(
            boolean nontokenOnly, boolean addIceCounter,
            CardEffect whenExiledEffect, boolean trackWithSource) {
        this(nontokenOnly, addIceCounter, whenExiledEffect, trackWithSource, 0);
    }

    public ExileOpponentCreaturesInsteadOfDyingEffect() {
        this(false, false, null, false, 0);
    }

    public static ExileOpponentCreaturesInsteadOfDyingEffect withIceCounter() {
        return new ExileOpponentCreaturesInsteadOfDyingEffect(true, true);
    }

    public static ExileOpponentCreaturesInsteadOfDyingEffect withSourceTracking() {
        return new ExileOpponentCreaturesInsteadOfDyingEffect(false, false, null, true);
    }

    public static ExileOpponentCreaturesInsteadOfDyingEffect withLifeGain(int amount) {
        return new ExileOpponentCreaturesInsteadOfDyingEffect(true, false, null, false, amount);
    }
}
