package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;

/**
 * On resolution, prompts the controller to choose one keyword from the given options,
 * then grants that keyword to the target permanent until end of turn.
 * Used by Golem Artisan's second ability: "{2}: Target artifact creature gains your choice of
 * flying, trample, or haste until end of turn."
 */
public record GrantChosenKeywordToTargetEffect(List<Keyword> options) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
