package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player chooses up to {@code maxCount} matching permanents they control, then all other
 * matching permanents are destroyed.
 *
 * <p>Choices are made in active-player order before the destruction happens simultaneously.
 */
public record EachPlayerChoosesPermanentsThenDestroyRestEffect(
        int maxCount, PermanentPredicate filter) implements BoardWipeEffect {

    public EachPlayerChoosesPermanentsThenDestroyRestEffect {
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount must not be negative");
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null");
        }
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
