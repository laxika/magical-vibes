package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses up to the configured number of lands they control, then sacrifices all
 * other lands.
 *
 * <p>The choices are made in active-player order before any land is sacrificed. A player with
 * fewer lands than the configured number chooses all of their lands.
 */
public record EachPlayerChoosesLandsThenSacrificeRestEffect(int landsToKeep) implements CardEffect {

    public EachPlayerChoosesLandsThenSacrificeRestEffect {
        if (landsToKeep < 0) {
            throw new IllegalArgumentException("landsToKeep must not be negative");
        }
    }
}
