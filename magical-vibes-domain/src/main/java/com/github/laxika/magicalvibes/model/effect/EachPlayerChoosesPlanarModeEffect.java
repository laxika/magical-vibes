package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** APNAP mode choices stored on the source face-up planar object. */
public record EachPlayerChoosesPlanarModeEffect(List<String> modes, boolean firstUpkeepOnly,
                                                 boolean switchExistingModes) implements CardEffect {

    public EachPlayerChoosesPlanarModeEffect {
        modes = List.copyOf(modes);
        if (modes.isEmpty()) {
            throw new IllegalArgumentException("At least one planar mode is required");
        }
    }
}
