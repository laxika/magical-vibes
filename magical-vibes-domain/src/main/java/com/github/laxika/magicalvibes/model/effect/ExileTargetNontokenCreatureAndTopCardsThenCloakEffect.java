package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;

/** Exiles the targeted nontoken creature and the controller's top cards, then cloaks them tapped. */
public record ExileTargetNontokenCreatureAndTopCardsThenCloakEffect() implements CardEffect {

    private static final PermanentAllOfPredicate TARGET_RESTRICTION = new PermanentAllOfPredicate(
            List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                    new PermanentOwnedBySourceControllerPredicate()));

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), TARGET_RESTRICTION);
    }
}
