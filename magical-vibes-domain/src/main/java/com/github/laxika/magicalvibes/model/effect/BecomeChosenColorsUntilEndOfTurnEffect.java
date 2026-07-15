package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * "Target permanent becomes the color or colors of your choice until end of turn"
 * (Prismwake Merrow). The controller picks one or more colors when this resolves; the chosen
 * colors replace the permanent's colors (CR 105.3, a layer-5 color-setting effect) until end of
 * turn.
 *
 * <p>The trigger/spell instance carries an empty {@link #colors} set — its handler begins the
 * multi-color choice. The choice handler then floats a second instance of this effect holding the
 * colors the controller actually picked, which the CR 613 layer engine applies (see
 * {@code LayerSystemService.applyL5Instance}).
 */
public record BecomeChosenColorsUntilEndOfTurnEffect(Set<CardColor> colors) implements CardEffect {

    public BecomeChosenColorsUntilEndOfTurnEffect {
        colors = new LinkedHashSet<>(colors);
    }

    public BecomeChosenColorsUntilEndOfTurnEffect() {
        this(Set.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
