package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;

/**
 * On resolution, prompts the controller to choose one keyword from the given options,
 * then grants that keyword to the second target permanent until end of turn.
 */
public record GrantChosenKeywordToSecondTargetEffect(List<Keyword> options) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
