package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Conditional wrapper: "as long as this creature has [keyword]".
 * The wrapped effect only applies while the source permanent has the specified keyword
 * (accounting for temporary keyword removal from activated abilities).
 */
public record SelfHasKeywordConditionalEffect(Keyword keyword, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "has " + keyword.getDisplayName();
    }

    @Override
    public String conditionNotMetReason() {
        return "does not have " + keyword.getDisplayName();
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
