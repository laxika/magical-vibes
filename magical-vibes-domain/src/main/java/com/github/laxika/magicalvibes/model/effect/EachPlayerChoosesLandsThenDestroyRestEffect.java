package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses up to the configured number of lands they control, then all other permanents
 * are destroyed together.
 *
 * <p>The choices are made in active-player order before any permanent is destroyed. A player with
 * fewer lands than the configured number chooses all of their lands.
 */
public record EachPlayerChoosesLandsThenDestroyRestEffect(int landsToKeep) implements BoardWipeEffect {

    public EachPlayerChoosesLandsThenDestroyRestEffect {
        if (landsToKeep < 0) {
            throw new IllegalArgumentException("landsToKeep must not be negative");
        }
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
