package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses any number of creatures they control with total power at most the limit, then
 * sacrifices the other creatures they control.
 */
public record EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect(int maxPower)
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
