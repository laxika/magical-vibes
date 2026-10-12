package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles every creature except creatures sharing a creature type with a creature that convoked the
 * resolving spell, together with every other permanent matching {@code alsoExile} (an artifact or
 * enchantment is exiled even if it is a creature that shares a type with a convoker). Everything
 * leaves the battlefield at the same time, so permanents that watch for another permanent leaving
 * see the whole group.
 *
 * @param alsoExile permanents exiled regardless of the convoke exception, or {@code null} for none
 */
public record ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect(PermanentPredicate alsoExile)
        implements BoardWipeEffect {

    public ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect() {
        this(null);
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
