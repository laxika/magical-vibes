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
 * this effect establishes the mutual bond Dance of Many needs: the source enchantment and the created
 * token each store the other's id in {@code chosenPermanentId}, and the token is given an
 * {@code ON_SELF_LEAVES_BATTLEFIELD} {@link RemoveLinkedPermanentEffect} so it sacrifices the enchantment
 * if it leaves. The enchantment carries the reciprocal exile trigger on its own card.
 */
public record CreateTokenCopyAndLinkToSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        )));
    }
}
