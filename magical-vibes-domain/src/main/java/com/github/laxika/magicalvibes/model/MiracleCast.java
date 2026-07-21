package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * Miracle (CR 702.94): when this card is drawn as the first card drawn this turn, its controller
 * may reveal it; revealing triggers an ability that lets them cast it by paying the miracle cost
 * rather than its mana cost (during that trigger's resolution, ignoring type-based timing).
 *
 * <p>The cost is declared on the card ({@code addCastingOption(new MiracleCast("{1}{U}"))});
 * Scryfall only supplies the {@link Keyword#MIRACLE} presence flag.
 */
public record MiracleCast(List<CastingCost> costs) implements CastingOption {

    public MiracleCast(String manaCost) {
        this(List.of(new ManaCastingCost(manaCost)));
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    public String manaCostString() {
        return getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost).orElse(null);
    }
}
