package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that this permanent
 * requires a basic land type choice as it enters the battlefield
 * ("As ~ enters, choose a basic land type." / "choose two basic land types.").
 *
 * @param choicesRequired how many basic land types to choose (1 for Convincing Mirage /
 *                        Phantasmal Terrain; 2 for Illusionary Terrain)
 */
public record ChooseBasicLandTypeOnEnterEffect(int choicesRequired) implements CardEffect {

    public ChooseBasicLandTypeOnEnterEffect() {
        this(1);
    }
}
