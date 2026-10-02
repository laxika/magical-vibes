package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each player sacrifices a matching permanent of their choice or loses life instead. */
public record EachPlayerSacrificesPermanentOrLosesLifeEffect(
        PermanentPredicate filter, DynamicAmount lifeLoss, String sacrificeDescription,
        boolean opponentsOnly, boolean mayChooseLife) implements CardEffect {

    public EachPlayerSacrificesPermanentOrLosesLifeEffect(PermanentPredicate filter, int lifeLoss) {
        this(filter, new Fixed(lifeLoss), "a permanent", false, true);
    }

    public EachPlayerSacrificesPermanentOrLosesLifeEffect(
            PermanentPredicate filter, int lifeLoss, String sacrificeDescription) {
        this(filter, new Fixed(lifeLoss), sacrificeDescription, false, true);
    }

    public static EachPlayerSacrificesPermanentOrLosesLifeEffect opponentsMustSacrifice(
            PermanentPredicate filter, DynamicAmount lifeLoss, String sacrificeDescription) {
        return new EachPlayerSacrificesPermanentOrLosesLifeEffect(
                filter, lifeLoss, sacrificeDescription, true, false);
    }
}
