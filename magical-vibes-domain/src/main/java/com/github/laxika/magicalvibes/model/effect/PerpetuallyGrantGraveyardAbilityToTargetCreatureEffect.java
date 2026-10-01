package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/** Perpetually grants a graveyard-activated ability to a target creature's card identity. */
public record PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect(
        ActivatedAbility ability, PermanentPredicate filter) implements CardEffect {

    public PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect(ActivatedAbility ability) {
        this(ability, new PermanentControlledBySourceControllerPredicate());
    }

    public PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect {
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(filter, "filter");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter);
    }
}
