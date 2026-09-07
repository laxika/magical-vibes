package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses one matching creature for each player; all other creatures are destroyed.
 */
public record ChooseCreatureForEachPlayerDestroyRestEffect(PermanentPredicate choiceFilter)
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
