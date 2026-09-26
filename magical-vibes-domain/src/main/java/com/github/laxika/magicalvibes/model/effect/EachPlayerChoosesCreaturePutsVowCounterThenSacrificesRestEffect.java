package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a creature they control, puts a vow counter on it, then sacrifices the
 * other creatures they control. Choices are made in APNAP order and the sacrifices are
 * simultaneous.
 */
public record EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect()
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
