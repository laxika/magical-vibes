package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/** Destroys creatures blocked by a targeted Wall and returns one creature per resulting death. */
public record DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.permanent(), new PermanentHasSubtypePredicate(CardSubtype.WALL));
    }
}
