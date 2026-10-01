package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/** Perpetually grants an activated ability to a target permanent's card identity. */
public record PerpetuallyGrantActivatedAbilityToTargetPermanentEffect(
        ActivatedAbility ability, PermanentPredicate filter) implements CardEffect {

    public PerpetuallyGrantActivatedAbilityToTargetPermanentEffect(ActivatedAbility ability) {
        this(ability, new PermanentControlledBySourceControllerPredicate());
    }

    public PerpetuallyGrantActivatedAbilityToTargetPermanentEffect {
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(filter, "filter");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), filter);
    }
}
