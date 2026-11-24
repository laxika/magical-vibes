package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

/**
 * Resolves Kaya, Spirits' Justice's exile trigger: the controller may choose a creature card from
 * among the stack entry's triggering cards (the creature cards that were put into exile), and the targeted
 * token becomes a copy of it until end of turn, except it has flying. The target is a token you control.
 */
public record KayaSpiritsJusticeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(), new PermanentControlledBySourceControllerPredicate())));
    }
}
