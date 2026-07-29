package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that this permanent
 * requires a basic land type choice as it enters the battlefield
 * ("As ~ enters, choose a basic land type." / "choose two basic land types.").
 *
 * @param choicesRequired how many basic land types to choose (1 for Convincing Mirage /
 *                        Phantasmal Terrain; 2 for Illusionary Terrain)
 * @param allowedTypes    the basic land types offered; empty means all five. Narrowed for cards
 *                        that restrict the choice ("choose Island or Swamp" — Roots of Life)
 */
public record ChooseBasicLandTypeOnEnterEffect(int choicesRequired, List<CardSubtype> allowedTypes) implements CardEffect {

    public ChooseBasicLandTypeOnEnterEffect() {
        this(1, List.of());
    }

    public ChooseBasicLandTypeOnEnterEffect(int choicesRequired) {
        this(choicesRequired, List.of());
    }

    public ChooseBasicLandTypeOnEnterEffect(List<CardSubtype> allowedTypes) {
        this(1, allowedTypes);
    }
}
