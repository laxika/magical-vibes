package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

public record CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                         Integer toughnessOverride,
                                         Set<CardType> additionalTypesOverride,
                                         List<ActivatedAbility> additionalActivatedAbilities) implements ReplacementEffect {

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel) {
        this(filter, typeLabel, null, null, Set.of(), List.of());
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride) {
        this(filter, typeLabel, powerOverride, toughnessOverride, Set.of(), List.of());
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        this(filter, typeLabel, powerOverride, toughnessOverride, additionalTypesOverride, List.of());
    }
}
