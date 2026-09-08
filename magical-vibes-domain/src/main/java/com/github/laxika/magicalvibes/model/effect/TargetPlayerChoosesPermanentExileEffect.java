package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A targeted player chooses a permanent they control matching {@code filter}, then it is exiled.
 */
public record TargetPlayerChoosesPermanentExileEffect(PermanentPredicate filter, String permanentLabel)
        implements CardEffect {

    public TargetPlayerChoosesPermanentExileEffect(PermanentPredicate filter) {
        this(filter, "permanent");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
