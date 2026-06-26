package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper: "as long as an opponent controls a permanent matching [filter]".
 */
public record OpponentControlsPermanentConditionalEffect(
        PermanentPredicate filter,
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "opponent controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent controls a matching permanent";
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }
}
