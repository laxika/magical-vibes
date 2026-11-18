package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * "When this enchantment enters, create a token that's a copy of target nontoken creature."
 *
 * <p>Beyond creating the token copy (of the permanent referenced by the stack entry's {@code targetId}),
 * this effect makes "the token" Dance of Many's other abilities name: every created token (several with a
 * token doubler, plus any token a replacement adds or substitutes, such as Queen Allenal's Soldier) is
 * recorded under the source in {@code GameData.sourceCreatedTokens}. The enchantment's own abilities read that
 * record: {@link ExileTokensCreatedWithSourceEffect} as it leaves, and "when the token leaves the battlefield,
 * sacrifice this enchantment" through {@code PermanentCreatedBySourcePredicate}.
 */
public record CreateTokenCopyAndLinkToSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        )));
    }
}
