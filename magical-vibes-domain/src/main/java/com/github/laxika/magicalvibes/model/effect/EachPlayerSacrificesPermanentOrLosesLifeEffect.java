package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each player sacrifices a matching permanent of their choice or loses life instead. */
public record EachPlayerSacrificesPermanentOrLosesLifeEffect(
        PermanentPredicate filter, int lifeLoss, String sacrificeDescription) implements CardEffect {

    public EachPlayerSacrificesPermanentOrLosesLifeEffect(PermanentPredicate filter, int lifeLoss) {
        this(filter, lifeLoss, "a permanent");
    }
}
